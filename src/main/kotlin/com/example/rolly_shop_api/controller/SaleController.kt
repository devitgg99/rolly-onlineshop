package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.SaleRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.service.SaleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
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
        summary = "Get all sales",
        description = "🔒 ADMIN ONLY - Get all sales (paginated)"
    )
    fun getAll(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") direction: String
    ): BaseResponse<PageResponse<SaleSimpleResponse>> {
        val sort = if (direction == "asc") Sort.by("createdAt").ascending() else Sort.by("createdAt").descending()
        val pageable = PageRequest.of(page, size, sort)
        return BaseResponse.success(saleService.getAll(pageable), "Sales retrieved")
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
}
