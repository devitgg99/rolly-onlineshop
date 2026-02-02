package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.Sale
import com.example.rolly_shop_api.model.entity.SaleItem
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.SaleRepository
import com.example.rolly_shop_api.repository.UserRepository
import com.example.rolly_shop_api.service.CurrentUserService
import com.example.rolly_shop_api.service.SaleService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.util.*

@Service
class SaleServiceImplement(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
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
}
