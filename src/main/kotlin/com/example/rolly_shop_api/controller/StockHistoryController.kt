package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.StockAdjustmentRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.AdjustmentType
import com.example.rolly_shop_api.service.StockHistoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Stock History", description = "Stock change history and audit trail")
class StockHistoryController(
    private val stockHistoryService: StockHistoryService
) {

    @GetMapping("/products/{productId}/stock-history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get stock change history for a product")
    fun getStockHistory(
        @PathVariable productId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): BaseResponse<PageResponse<StockHistoryResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val startInstant = startDate?.let { Instant.parse(it) }
        val endInstant = endDate?.let { Instant.parse(it) }
        
        val historyPage = stockHistoryService.getStockHistory(
            productId,
            startInstant,
            endInstant,
            pageable
        )
        
        val pageResponse = PageResponse(
            content = historyPage.content,
            page = historyPage.number,
            size = historyPage.size,
            totalElements = historyPage.totalElements,
            totalPages = historyPage.totalPages,
            isFirst = historyPage.isFirst,
            isLast = historyPage.isLast
        )
        
        return BaseResponse.success(
            data = pageResponse,
            message = "Stock history retrieved successfully"
        )
    }

    @PostMapping("/products/{productId}/stock-adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually adjust stock (creates history entry)")
    fun adjustStock(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: StockAdjustmentRequest
    ): BaseResponse<StockAdjustmentResponse> {
        val result = stockHistoryService.adjustStock(productId, request)
        return BaseResponse.success(
            data = result,
            message = "Stock adjusted successfully"
        )
    }

    @GetMapping("/products/stock-history/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get stock history summary (overview of all products)")
    fun getStockHistorySummary(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(required = false) adjustmentType: AdjustmentType?
    ): BaseResponse<StockHistorySummaryResponse> {
        val startInstant = Instant.parse(startDate)
        val endInstant = Instant.parse(endDate)
        
        val summary = stockHistoryService.getStockHistorySummary(
            startInstant,
            endInstant,
            adjustmentType
        )
        
        return BaseResponse.success(
            data = summary,
            message = "Stock history summary retrieved"
        )
    }
}
