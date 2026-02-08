package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.RefundRequest
import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.*
import com.example.rolly_shop_api.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.example.rolly_shop_api.service.CurrentUserService
import com.example.rolly_shop_api.service.SaleService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SaleServiceImplement(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService,
    private val saleItemRepository: SaleItemRepository,
    private val refundRepository: RefundRepository,
    private val refundItemRepository: RefundItemRepository
) : SaleService {

    @Transactional
    override fun createSale(request: SaleRequest): SaleResponse {
        // Get the admin making the sale
        val adminId = currentUserService.getCurrentUserId()
        val admin = userRepository.findById(adminId).orElse(null)

        // Calculate totals first
        var totalAmount = BigDecimal.ZERO
        var totalCost = BigDecimal.ZERO

        // Prepare item data (validate stock and calculate prices)
        data class ItemData(
            val product: com.example.rolly_shop_api.model.entity.Product,
            val quantity: Int,
            val unitPrice: BigDecimal,
            val unitCost: BigDecimal,
            val subtotal: BigDecimal,
            val profit: BigDecimal
        )

        val itemDataList = mutableListOf<ItemData>()

        for (itemRequest in request.items) {
            val product = productRepository.findById(itemRequest.productId)
                .orElseThrow { NoSuchElementException("Product not found: ${itemRequest.productId}") }

            // Check stock
            if (product.stockQuantity < itemRequest.quantity) {
                throw IllegalStateException("Insufficient stock for ${product.name}. Available: ${product.stockQuantity}, Requested: ${itemRequest.quantity}")
            }

            // Calculate prices (use discounted price for selling)
            val unitPrice = product.getDiscountedPrice()
            val unitCost = product.costPrice
            val subtotal = unitPrice.multiply(BigDecimal(itemRequest.quantity))
            val itemProfit = unitPrice.subtract(unitCost).multiply(BigDecimal(itemRequest.quantity))

            itemDataList.add(ItemData(product, itemRequest.quantity, unitPrice, unitCost, subtotal, itemProfit))

            // Update totals
            totalAmount = totalAmount.add(subtotal)
            totalCost = totalCost.add(unitCost.multiply(BigDecimal(itemRequest.quantity)))

            // Deduct stock
            product.stockQuantity -= itemRequest.quantity
            productRepository.save(product)
        }

        // Apply discount
        val finalAmount = totalAmount.subtract(request.discountAmount)
        val finalProfit = finalAmount.subtract(totalCost)

        // Create sale with final values
        val sale = Sale(
            customerName = request.customerName,
            customerPhone = request.customerPhone,
            totalAmount = finalAmount,
            totalCost = totalCost,
            profit = finalProfit,
            discountAmount = request.discountAmount,
            paymentMethod = request.paymentMethod,
            soldBy = admin,
            notes = request.notes
        )

        // Save sale first to get ID
        val savedSale = saleRepository.save(sale)

        // Now create and add items with the saved sale reference
        for (itemData in itemDataList) {
            val saleItem = SaleItem(
                sale = savedSale,
                product = itemData.product,
                quantity = itemData.quantity,
                unitPrice = itemData.unitPrice,
                unitCost = itemData.unitCost,
                subtotal = itemData.subtotal,
                profit = itemData.profit
            )
            savedSale.items.add(saleItem)
        }

        // Save again to persist items (cascade)
        val finalSale = saleRepository.save(savedSale)

        return SaleResponse.from(finalSale)
    }

    override fun getById(id: UUID): SaleResponse {
        val sale = saleRepository.findById(id)
            .orElseThrow { NoSuchElementException("Sale not found") }
        return SaleResponse.from(sale)
    }

    override fun getAll(pageable: Pageable): PageResponse<SaleSimpleResponse> {
        val page = saleRepository.findAll(pageable)
        return PageResponse.from(page) { SaleSimpleResponse.from(it) }
    }

    override fun getTodaySales(pageable: Pageable): PageResponse<SaleSimpleResponse> {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val page = saleRepository.findTodaySales(startOfDay, pageable)
        return PageResponse.from(page) { SaleSimpleResponse.from(it) }
    }

    override fun getSalesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): PageResponse<SaleSimpleResponse> {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val page = saleRepository.findByCreatedAtBetween(start, end, pageable)
        return PageResponse.from(page) { SaleSimpleResponse.from(it) }
    }

    override fun getSummary(startDate: LocalDate, endDate: LocalDate): SalesSummaryResponse {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val totalSales = saleRepository.countByCreatedAtBetween(start, end)
        val totalRevenue = saleRepository.getTotalRevenue(start, end)
        val totalProfit = saleRepository.getTotalProfit(start, end)
        val totalCost = totalRevenue.subtract(totalProfit)

        val profitMargin = if (totalRevenue > BigDecimal.ZERO) {
            totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toDouble()
        } else 0.0

        return SalesSummaryResponse(
            totalSales = totalSales,
            totalRevenue = totalRevenue,
            totalCost = totalCost,
            totalProfit = totalProfit,
            profitMargin = profitMargin,
            periodStart = start,
            periodEnd = end
        )
    }

    override fun getTodaySummary(): SalesSummaryResponse {
        val today = LocalDate.now()
        return getSummary(today, today)
    }

    // ==================== PRODUCT SALES ANALYTICS ====================

    override fun getProductSalesStats(productId: UUID): ProductSalesStatsResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found") }

        val totalQuantitySold = saleItemRepository.getTotalQuantitySold(productId)
        val totalRevenue = saleItemRepository.getTotalRevenueForProduct(productId)
        val totalProfit = saleItemRepository.getTotalProfitForProduct(productId)

        return ProductSalesStatsResponse(
            productId = product.id!!,
            productName = product.name,
            totalQuantitySold = totalQuantitySold,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit,
            currentStock = product.stockQuantity
        )
    }

    override fun getTopSellingProducts(limit: Int): List<TopSellingProductResponse> {
        val results = saleItemRepository.getTopSellingProducts(PageRequest.of(0, limit))
        return results.map { row ->
            TopSellingProductResponse(
                productId = row[0] as UUID,
                productName = row[1] as String,
                totalQuantitySold = row[2] as Long
            )
        }
    }

    override fun getTopSellingProductsBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<TopSellingProductResponse> {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val results = saleItemRepository.getTopSellingProductsBetween(start, end, PageRequest.of(0, limit))
        return results.map { row ->
            TopSellingProductResponse(
                productId = row[0] as UUID,
                productName = row[1] as String,
                totalQuantitySold = row[2] as Long
            )
        }
    }

    // ==================== SALES ANALYTICS DASHBOARD ====================

    override fun getSalesAnalytics(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String
    ): SalesAnalyticsDashboardResponse {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        // Basic metrics
        val totalSales = saleRepository.countByCreatedAtBetween(start, end)
        val totalRevenue = saleRepository.getTotalRevenue(start, end)
        val totalProfit = saleRepository.getTotalProfit(start, end)
        val avgOrderValue = if (totalSales > 0) {
            totalRevenue.divide(BigDecimal(totalSales), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        // Sales by period
        val salesByDay = calculateSalesByPeriod(start, end, groupBy)

        // Sales by payment method
        val salesByPaymentMethod = calculateSalesByPaymentMethod(start, end)

        // Sales by hour
        val salesByHour = calculateSalesByHour(start, end)

        // Top customers
        val topCustomers = calculateTopCustomers(start, end)

        // Profit margin trend
        val profitMarginTrend = calculateProfitMarginTrend(start, end, groupBy)

        return SalesAnalyticsDashboardResponse(
            totalSales = totalSales,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit,
            avgOrderValue = avgOrderValue,
            salesByDay = salesByDay,
            salesByPaymentMethod = salesByPaymentMethod,
            salesByHour = salesByHour,
            topCustomers = topCustomers,
            profitMarginTrend = profitMarginTrend
        )
    }

    private fun calculateSalesByPeriod(start: Instant, end: Instant, groupBy: String): List<SalesByPeriodResponse> {
        val allSales = saleRepository.findByCreatedAtBetween(start, end, Pageable.unpaged()).content
        
        val formatter = when (groupBy.lowercase()) {
            "week" -> DateTimeFormatter.ofPattern("yyyy-'W'ww")
            "month" -> DateTimeFormatter.ofPattern("yyyy-MM")
            else -> DateTimeFormatter.ofPattern("yyyy-MM-dd") // day
        }

        val groupedSales = allSales.groupBy { sale ->
            val localDateTime = LocalDateTime.ofInstant(sale.createdAt, ZoneId.systemDefault())
            localDateTime.format(formatter)
        }

        return groupedSales.map { (date, sales) ->
            SalesByPeriodResponse(
                date = date,
                sales = sales.size.toLong(),
                revenue = sales.sumOf { it.totalAmount },
                profit = sales.sumOf { it.profit }
            )
        }.sortedBy { it.date }
    }

    private fun calculateSalesByPaymentMethod(start: Instant, end: Instant): Map<PaymentMethod, PaymentMethodSalesResponse> {
        val results = saleRepository.getSalesByPaymentMethod(start, end)
        return results.associate { row ->
            val paymentMethod = row[0] as PaymentMethod
            val count = (row[1] as Long)
            val revenue = row[2] as BigDecimal
            paymentMethod to PaymentMethodSalesResponse(count, revenue)
        }
    }

    private fun calculateSalesByHour(start: Instant, end: Instant): List<SalesByHourResponse> {
        val results = saleRepository.getSalesByHour(start, end)
        return results.map { row ->
            SalesByHourResponse(
                hour = (row[0] as Number).toInt(),
                sales = (row[1] as Long),
                revenue = row[2] as BigDecimal
            )
        }
    }

    private fun calculateTopCustomers(start: Instant, end: Instant): List<TopCustomerResponse> {
        val results = saleRepository.getTopCustomers(start, end, PageRequest.of(0, 10))
        return results.map { row ->
            TopCustomerResponse(
                name = row[0] as String,
                phone = row[1] as? String,
                totalSpent = row[2] as BigDecimal,
                orderCount = row[3] as Long
            )
        }
    }

    private fun calculateProfitMarginTrend(start: Instant, end: Instant, groupBy: String): List<ProfitMarginTrendResponse> {
        val allSales = saleRepository.findByCreatedAtBetween(start, end, Pageable.unpaged()).content
        
        val formatter = when (groupBy.lowercase()) {
            "week" -> DateTimeFormatter.ofPattern("yyyy-'W'ww")
            "month" -> DateTimeFormatter.ofPattern("yyyy-MM")
            else -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }

        val groupedSales = allSales.groupBy { sale ->
            val localDateTime = LocalDateTime.ofInstant(sale.createdAt, ZoneId.systemDefault())
            localDateTime.format(formatter)
        }

        return groupedSales.map { (date, sales) ->
            val totalRevenue = sales.sumOf { it.totalAmount }
            val totalProfit = sales.sumOf { it.profit }
            val margin = if (totalRevenue > BigDecimal.ZERO) {
                totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100)).toDouble()
            } else 0.0

            ProfitMarginTrendResponse(date = date, margin = margin)
        }.sortedBy { it.date }
    }

    // ==================== ADVANCED FILTERING ====================

    override fun getSalesWithFilters(
        startDate: LocalDate?,
        endDate: LocalDate?,
        paymentMethod: PaymentMethod?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
        customerName: String?,
        productId: UUID?,
        sortBy: String,
        direction: String,
        pageable: Pageable
    ): PageResponse<SaleSimpleResponse> {
        val start = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
        val end = endDate?.plusDays(1)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

        val sortField = when (sortBy.lowercase()) {
            "amount" -> "totalAmount"
            "profit" -> "profit"
            else -> "createdAt"
        }

        val sort = if (direction.lowercase() == "asc") {
            Sort.by(sortField).ascending()
        } else {
            Sort.by(sortField).descending()
        }

        val pageableWithSort = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)

        // Use Specification for dynamic filtering
        val spec = SaleSpecifications.withFilters(
            start, end, paymentMethod, minAmount, maxAmount, customerName, productId
        )
        
        val page = saleRepository.findAll(spec, pageableWithSort)

        return PageResponse.from(page) { SaleSimpleResponse.from(it) }
    }

    // ==================== REFUND MANAGEMENT ====================

    @Transactional
    override fun createRefund(saleId: UUID, request: RefundRequest): RefundResponse {
        val sale = saleRepository.findById(saleId)
            .orElseThrow { NoSuchElementException("Sale not found") }

        val adminId = currentUserService.getCurrentUserId()
        val admin = userRepository.findById(adminId).orElse(null)

        // Calculate refund amount
        var refundAmount = BigDecimal.ZERO

        // Prepare refund item data
        data class RefundItemData(
            val product: Product,
            val quantity: Int,
            val unitPrice: BigDecimal,
            val subtotal: BigDecimal,
            val reason: String
        )

        val refundItemDataList = mutableListOf<RefundItemData>()

        for (itemRequest in request.items) {
            // Find the original sale item
            val saleItem = sale.items.find { it.product.id == itemRequest.productId }
                ?: throw NoSuchElementException("Product not found in original sale")

            // Validate quantity
            if (itemRequest.quantity > saleItem.quantity) {
                throw IllegalStateException("Refund quantity exceeds original quantity")
            }

            val product = productRepository.findById(itemRequest.productId)
                .orElseThrow { NoSuchElementException("Product not found") }

            val subtotal = saleItem.unitPrice.multiply(BigDecimal(itemRequest.quantity))
            refundAmount = refundAmount.add(subtotal)

            refundItemDataList.add(
                RefundItemData(
                    product = product,
                    quantity = itemRequest.quantity,
                    unitPrice = saleItem.unitPrice,
                    subtotal = subtotal,
                    reason = itemRequest.reason
                )
            )

            // Restore stock
            product.stockQuantity += itemRequest.quantity
            productRepository.save(product)
        }

        // Create refund
        val refund = Refund(
            sale = sale,
            refundAmount = refundAmount,
            refundMethod = request.refundMethod,
            processedBy = admin,
            notes = request.notes
        )

        val savedRefund = refundRepository.save(refund)

        // Create refund items
        for (itemData in refundItemDataList) {
            val refundItem = RefundItem(
                refund = savedRefund,
                product = itemData.product,
                quantity = itemData.quantity,
                unitPrice = itemData.unitPrice,
                subtotal = itemData.subtotal,
                reason = itemData.reason
            )
            savedRefund.items.add(refundItem)
        }

        val finalRefund = refundRepository.save(savedRefund)
        return RefundResponse.from(finalRefund)
    }

    override fun getAllRefunds(pageable: Pageable): PageResponse<RefundSimpleResponse> {
        val page = refundRepository.findAll(pageable)
        return PageResponse.from(page) { RefundSimpleResponse.from(it) }
    }

    override fun getRefundsBySale(saleId: UUID): List<RefundResponse> {
        val refunds = refundRepository.findBySaleId(saleId)
        return refunds.map { RefundResponse.from(it) }
    }

    // ==================== EXPORT ====================

    override fun exportSales(
        format: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        paymentMethod: PaymentMethod?,
        includeItems: Boolean
    ): ByteArray {
        val start = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
        val end = endDate?.plusDays(1)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

        val sales = if (start != null && end != null) {
            saleRepository.findByCreatedAtBetween(start, end, Pageable.unpaged()).content
        } else {
            saleRepository.findAll()
        }.filter { sale ->
            paymentMethod == null || sale.paymentMethod == paymentMethod
        }

        return when (format.lowercase()) {
            "csv" -> generateCSV(sales, includeItems)
            "excel" -> generateExcel(sales, includeItems)
            "pdf" -> generatePDF(sales, includeItems)
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }

    private fun generateCSV(sales: List<Sale>, includeItems: Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        output.bufferedWriter().use { writer ->
            // Header
            if (includeItems) {
                writer.write("Date,Time,Invoice#,Customer,Product,Quantity,Unit Price,Subtotal,Payment Method,Discount,Total,Profit,Status\n")
                
                sales.forEach { sale ->
                    val dateTime = LocalDateTime.ofInstant(sale.createdAt, ZoneId.systemDefault())
                    val date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val customer = sale.customerName ?: "Walk-in"
                    val hasRefund = refundRepository.existsBySaleId(sale.id!!)
                    val status = if (hasRefund) "Refunded" else "Completed"

                    sale.items.forEach { item ->
                        writer.write("$date,$time,${sale.id},\"$customer\",\"${item.product.name}\",${item.quantity},${item.unitPrice},${item.subtotal},${sale.paymentMethod},${sale.discountAmount},${sale.totalAmount},${sale.profit},$status\n")
                    }
                }
            } else {
                writer.write("Date,Time,Invoice#,Customer,Items,Payment Method,Subtotal,Discount,Tax,Total,Profit,Status\n")
                
                sales.forEach { sale ->
                    val dateTime = LocalDateTime.ofInstant(sale.createdAt, ZoneId.systemDefault())
                    val date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val customer = sale.customerName ?: "Walk-in"
                    val itemCount = sale.items.size
                    val hasRefund = refundRepository.existsBySaleId(sale.id!!)
                    val status = if (hasRefund) "Refunded" else "Completed"
                    val subtotal = sale.totalAmount.add(sale.discountAmount)

                    writer.write("$date,$time,${sale.id},\"$customer\",$itemCount,${sale.paymentMethod},$subtotal,${sale.discountAmount},0.00,${sale.totalAmount},${sale.profit},$status\n")
                }
            }
        }
        return output.toByteArray()
    }

    private fun generateExcel(sales: List<Sale>, includeItems: Boolean): ByteArray {
        // For now, return CSV format
        // In production, you would use Apache POI library
        return generateCSV(sales, includeItems)
    }

    private fun generatePDF(sales: List<Sale>, includeItems: Boolean): ByteArray {
        // For now, return CSV format
        // In production, you would use iText or similar library
        return generateCSV(sales, includeItems)
    }
}
