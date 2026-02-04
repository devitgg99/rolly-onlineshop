package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.AdjustmentType
import com.example.rolly_shop_api.model.entity.ReferenceType
import com.example.rolly_shop_api.model.entity.StockHistory
import java.time.Instant

data class StockHistoryResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val previousStock: Int,
    val newStock: Int,
    val adjustment: Int,
    val adjustmentType: AdjustmentType,
    val reason: String?,
    val referenceId: String?,
    val referenceType: ReferenceType?,
    val updatedBy: String,
    val updatedByName: String,
    val createdAt: Instant
) {
    companion object {
        fun from(history: StockHistory): StockHistoryResponse {
            return StockHistoryResponse(
                id = history.id.toString(),
                productId = history.product.id.toString(),
                productName = history.productName,
                previousStock = history.previousStock,
                newStock = history.newStock,
                adjustment = history.adjustment,
                adjustmentType = history.adjustmentType,
                reason = history.reason,
                referenceId = history.referenceId?.toString(),
                referenceType = history.referenceType,
                updatedBy = history.updatedBy,
                updatedByName = history.updatedByName,
                createdAt = history.createdAt
            )
        }
    }
}

data class StockAdjustmentResponse(
    val productId: String,
    val previousStock: Int,
    val newStock: Int,
    val adjustment: Int,
    val adjustmentType: AdjustmentType,
    val reason: String?,
    val historyEntryId: String,
    val updatedBy: String,
    val updatedAt: Instant
)

data class StockHistorySummaryResponse(
    val totalAdjustments: Long,
    val byType: Map<String, Long>,
    val topProducts: List<TopProductAdjustment>
)

data class TopProductAdjustment(
    val productId: String,
    val productName: String,
    val totalAdjustments: Long,
    val netChange: Int
)
