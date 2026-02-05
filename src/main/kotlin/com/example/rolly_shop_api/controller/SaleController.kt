package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.RefundRequest
import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.PaymentMethod
import com.example.rolly_shop_api.service.SaleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Sales (POS)", description = "🔒 ADMIN ONLY - Walk-in sales / Point of Sale endpoints")
class SaleController(
    private val saleService: SaleService
) {

    @PostMapping
    @Operation(
        summary = "Create a walk-in sale",
        description = """
            🔒 ADMIN ONLY - Record a walk-in sale when customer buys products in-store.
            
            This will:
            - Record the sale with customer info (optional)
            - Calculate profit (selling price - cost price)
            - Deduct stock automatically
            - Track which admin made the sale
        """
    )
    fun createSale(@Valid @RequestBody request: SaleRequest): BaseResponse<SaleResponse> =
        BaseResponse.success(saleService.createSale(request), "Sale recorded successfully")

    @GetMapping("/{id}")
    @Operation(
        summary = "Get sale details",
        description = "🔒 ADMIN ONLY - Get full details of a sale including items and profit"
    )
    fun getById(@PathVariable id: UUID): BaseResponse<SaleResponse> =
        BaseResponse.success(saleService.getById(id), "Sale found")

    @GetMapping
    @Operation(
        summary = "Get all sales with advanced filters",
        description = """
            🔒 ADMIN ONLY - Get all sales with advanced filtering and sorting.
            
            Filters available:
            - Date range (startDate, endDate)
            - Payment method (CASH, CARD, E_WALLET, BANK_TRANSFER, COD)
            - Amount range (minAmount, maxAmount)
            - Customer name search (partial match)
            - Product filter (sales containing specific product)
            - Custom sorting by date, amount, or profit
        """
    )
    fun getAll(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @Parameter(description = "Payment method filter (CASH, CARD, E_WALLET, BANK_TRANSFER, COD)")
        @RequestParam(required = false) paymentMethod: PaymentMethod?,
        @Parameter(description = "Minimum sale amount")
        @RequestParam(required = false) minAmount: BigDecimal?,
        @Parameter(description = "Maximum sale amount")
        @RequestParam(required = false) maxAmount: BigDecimal?,
        @Parameter(description = "Customer name (partial match, case-insensitive)")
        @RequestParam(required = false) customerName: String?,
        @Parameter(description = "Product ID (find sales containing this product)")
        @RequestParam(required = false) productId: UUID?,
        @Parameter(description = "Sort by: date, amount, or profit")
        @RequestParam(defaultValue = "date") sortBy: String,
        @Parameter(description = "Sort direction: asc or desc")
        @RequestParam(defaultValue = "desc") direction: String,
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Items per page")
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<SaleSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(
            saleService.getSalesWithFilters(
                startDate, endDate, paymentMethod, minAmount, maxAmount,
                customerName, productId, sortBy, direction, pageable
            ),
            "Sales retrieved"
        )
    }

    @GetMapping("/today")
    @Operation(
        summary = "Get today's sales",
        description = "🔒 ADMIN ONLY - Get all sales made today"
    )
    fun getTodaySales(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<SaleSimpleResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(saleService.getTodaySales(pageable), "Today's sales")
    }

    @GetMapping("/range")
    @Operation(
        summary = "Get sales by date range",
        description = "🔒 ADMIN ONLY - Get sales within a date range"
    )
    fun getSalesByDateRange(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<SaleSimpleResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(saleService.getSalesByDateRange(startDate, endDate, pageable), "Sales in range")
    }

    // ==================== DASHBOARD / SUMMARY ====================

    @GetMapping("/summary/today")
    @Operation(
        summary = "Get today's sales summary",
        description = """
            🔒 ADMIN ONLY - Get today's sales dashboard:
            - Total number of sales
            - Total revenue (money received)
            - Total cost (what you paid for products)
            - Total profit
            - Profit margin percentage
        """
    )
    fun getTodaySummary(): BaseResponse<SalesSummaryResponse> =
        BaseResponse.success(saleService.getTodaySummary(), "Today's summary")

    @GetMapping("/summary")
    @Operation(
        summary = "Get sales summary for date range",
        description = "🔒 ADMIN ONLY - Get sales summary/dashboard for a specific date range"
    )
    fun getSummary(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): BaseResponse<SalesSummaryResponse> =
        BaseResponse.success(saleService.getSummary(startDate, endDate), "Sales summary")

    // ==================== PRODUCT SALES ANALYTICS ====================

    @GetMapping("/product/{productId}/stats")
    @Operation(
        summary = "Get sales stats for a product",
        description = """
            🔒 ADMIN ONLY - Get sales statistics for a specific product:
            - Total quantity sold (all time)
            - Total revenue from this product
            - Total profit from this product
            - Current stock remaining
        """
    )
    fun getProductSalesStats(@PathVariable productId: UUID): BaseResponse<ProductSalesStatsResponse> =
        BaseResponse.success(saleService.getProductSalesStats(productId), "Product sales stats")

    @GetMapping("/top-selling")
    @Operation(
        summary = "Get top selling products",
        description = "🔒 ADMIN ONLY - Get the best selling products by quantity sold (all time)"
    )
    fun getTopSellingProducts(
        @Parameter(description = "Number of products to return") @RequestParam(defaultValue = "10") limit: Int
    ): BaseResponse<List<TopSellingProductResponse>> =
        BaseResponse.success(saleService.getTopSellingProducts(limit), "Top selling products")

    @GetMapping("/top-selling/range")
    @Operation(
        summary = "Get top selling products in date range",
        description = "🔒 ADMIN ONLY - Get the best selling products within a specific date range"
    )
    fun getTopSellingProductsBetween(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @Parameter(description = "Number of products to return") @RequestParam(defaultValue = "10") limit: Int
    ): BaseResponse<List<TopSellingProductResponse>> =
        BaseResponse.success(saleService.getTopSellingProductsBetween(startDate, endDate, limit), "Top selling products in range")

    // ==================== SALES ANALYTICS DASHBOARD ====================

    @GetMapping("/analytics")
    
    @Operation(
        summary = "Get comprehensive sales analytics",
        description = """
            🔒 ADMIN ONLY - Get comprehensive sales dashboard analytics including:
            - Total sales, revenue, profit, avg order value
            - Sales by day/week/month
            - Sales by payment method
            - Sales by hour of day
            - Top customers
            - Profit margin trends
        """
    )
    fun getSalesAnalytics(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @Parameter(description = "Group by: day, week, or month")
        @RequestParam(defaultValue = "day") groupBy: String
    ): BaseResponse<SalesAnalyticsDashboardResponse> =
        BaseResponse.success(
            saleService.getSalesAnalytics(startDate, endDate, groupBy),
            "Sales analytics retrieved"
        )

    // ==================== ADVANCED FILTERING ====================

    @GetMapping("/filter")
    @Operation(
        summary = "Get sales with advanced filters",
        description = """
            🔒 ADMIN ONLY - Search and filter sales with multiple criteria:
            - Date range
            - Payment method
            - Amount range
            - Customer name
            - Specific product
            - Custom sorting
        """
    )
    fun getSalesWithFilters(
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @Parameter(description = "Payment method filter")
        @RequestParam(required = false) paymentMethod: PaymentMethod?,
        @Parameter(description = "Minimum amount")
        @RequestParam(required = false) minAmount: BigDecimal?,
        @Parameter(description = "Maximum amount")
        @RequestParam(required = false) maxAmount: BigDecimal?,
        @Parameter(description = "Customer name (partial match)")
        @RequestParam(required = false) customerName: String?,
        @Parameter(description = "Product ID (sales containing this product)")
        @RequestParam(required = false) productId: UUID?,
        @Parameter(description = "Sort by: date, amount, or profit")
        @RequestParam(defaultValue = "date") sortBy: String,
        @Parameter(description = "Sort direction: asc or desc")
        @RequestParam(defaultValue = "desc") direction: String,
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Items per page")
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<SaleSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(
            saleService.getSalesWithFilters(
                startDate, endDate, paymentMethod, minAmount, maxAmount,
                customerName, productId, sortBy, direction, pageable
            ),
            "Filtered sales retrieved"
        )
    }

    // ==================== REFUND & RETURN MANAGEMENT ====================

    @PostMapping("/{saleId}/refund")
    @Operation(
        summary = "Create a refund for a sale",
        description = """
            🔒 ADMIN ONLY - Process a refund for items in a sale.
            This will:
            - Record the refund with reason
            - Restore stock for refunded items
            - Track which admin processed the refund
        """
    )
    fun createRefund(
        @PathVariable saleId: UUID,
        @Valid @RequestBody request: RefundRequest
    ): BaseResponse<RefundResponse> =
        BaseResponse.success(
            saleService.createRefund(saleId, request),
            "Refund processed successfully"
        )

    @GetMapping("/refunds")
    @Operation(
        summary = "Get all refunds",
        description = "🔒 ADMIN ONLY - Get all refunds with pagination"
    )
    fun getAllRefunds(
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Items per page")
        @RequestParam(defaultValue = "20") size: Int
    ): BaseResponse<PageResponse<RefundSimpleResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(saleService.getAllRefunds(pageable), "Refunds retrieved")
    }

    @GetMapping("/{saleId}/refunds")
    @Operation(
        summary = "Get refunds for a specific sale",
        description = "🔒 ADMIN ONLY - Get all refunds associated with a specific sale"
    )
    fun getRefundsBySale(@PathVariable saleId: UUID): BaseResponse<List<RefundResponse>> =
        BaseResponse.success(saleService.getRefundsBySale(saleId), "Sale refunds retrieved")

    // ==================== EXPORT & REPORTING ====================

    @GetMapping("/export")
    @Operation(
        summary = "Export sales data",
        description = """
            🔒 ADMIN ONLY - Export sales data in various formats.
            Supports CSV, Excel, and PDF formats.
            Can include detailed item breakdown or summary only.
        """
    )
    fun exportSales(
        @Parameter(description = "Export format: csv, excel, or pdf")
        @RequestParam(defaultValue = "csv") format: String,
        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @Parameter(description = "Payment method filter")
        @RequestParam(required = false) paymentMethod: PaymentMethod?,
        @Parameter(description = "Include item breakdown")
        @RequestParam(defaultValue = "false") includeItems: Boolean
    ): ResponseEntity<ByteArray> {
        val data = saleService.exportSales(format, startDate, endDate, paymentMethod, includeItems)
        
        val contentType = when (format.lowercase()) {
            "csv" -> "text/csv"
            "excel" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
        
        val filename = "sales_export_${LocalDate.now()}.$format"
        
        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType(contentType)
            setContentDispositionFormData("attachment", filename)
        }
        
        return ResponseEntity(data, headers, HttpStatus.OK)
    }
}
