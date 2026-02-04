package com.example.rolly_shop_api.model.dto.request

import com.example.rolly_shop_api.model.entity.AdjustmentType
import jakarta.validation.constraints.NotNull

data class StockAdjustmentRequest(
    @field:NotNull(message = "Adjustment value is required")
    val adjustment: Int,
    
    @field:NotNull(message = "Adjustment type is required")
    val adjustmentType: AdjustmentType,
    
    val reason: String? = null
)
