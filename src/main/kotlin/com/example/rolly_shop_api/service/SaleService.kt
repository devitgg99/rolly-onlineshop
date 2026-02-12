package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.RefundRequest
import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.PaymentMethod
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface SaleService {
    // Create a new walk-in sale
    fun createSale(request: SaleRequest): SaleResponse

    // Get sale by ID
    fun getById(id: UUID): SaleResponse

    // Get all sales (paginated)
    fun getAll(pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get today's sales
    fun getTodaySales(pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get sales by date range
    fun getSalesByDateRange(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): PageResponse<SaleSimpleResponse>

    // Get sales summary (dashboard) - supports all-time or date range
    fun getSummary(startDate: LocalDate?, endDate: LocalDate?): SalesSummaryResponse

    // Get today's summary
    fun getTodaySummary(): SalesSummaryResponse

    // ==================== PRODUCT SALES ANALYTICS ====================

    // Get sales stats for a specific product
    fun getProductSalesStats(productId: UUID): ProductSalesStatsResponse

    // Get top selling products
    fun getTopSellingProducts(limit: Int): List<TopSellingProductResponse>

    // Get top selling products within date range
    fun getTopSellingProductsBetween(startDate: LocalDate, endDate: LocalDate, limit: Int): List<TopSellingProductResponse>

    // ==================== SALES ANALYTICS DASHBOARD ====================

    // Get comprehensive analytics dashboard
    fun getSalesAnalytics(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String
    ): SalesAnalyticsDashboardResponse

    // ==================== ADVANCED FILTERING ====================

    // Get sales with advanced filters
    fun getSalesWithFilters(
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
    ): PageResponse<SaleSimpleResponse>

    // ==================== REFUND MANAGEMENT ====================

    // Create a refund for a sale
    fun createRefund(saleId: UUID, request: RefundRequest): RefundResponse

    // Get all refunds
    fun getAllRefunds(pageable: Pageable): PageResponse<RefundSimpleResponse>

    // Get refunds for a specific sale
    fun getRefundsBySale(saleId: UUID): List<RefundResponse>

    // ==================== EXPORT ====================

    // Export sales data (returns file content as ByteArray)
    fun exportSales(
        format: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        paymentMethod: PaymentMethod?,
        includeItems: Boolean
    ): ByteArray
}
