package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.StockAdjustmentRequest
import com.example.rolly_shop_api.model.dto.response.StockAdjustmentResponse
import com.example.rolly_shop_api.model.dto.response.StockHistoryResponse
import com.example.rolly_shop_api.model.dto.response.StockHistorySummaryResponse
import com.example.rolly_shop_api.model.entity.AdjustmentType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

interface StockHistoryService {
    fun getStockHistory(
        productId: UUID,
        startDate: Instant?,
        endDate: Instant?,
        pageable: Pageable
    ): Page<StockHistoryResponse>
    
    fun adjustStock(productId: UUID, request: StockAdjustmentRequest): StockAdjustmentResponse
    
    fun getStockHistorySummary(
        startDate: Instant,
        endDate: Instant,
        adjustmentType: AdjustmentType?
    ): StockHistorySummaryResponse
}
