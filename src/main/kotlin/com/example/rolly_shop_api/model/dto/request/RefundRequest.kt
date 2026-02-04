package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Request to create a refund for a sale
 */
data class RefundRequest(
    @field:NotEmpty(message = "At least one item is required for refund")
    val items: List<RefundItemRequest>,
    
    val refundMethod: RefundMethod = RefundMethod.CASH,
    
    val notes: String? = null
)

data class RefundItemRequest(
    val productId: UUID,
    val quantity: Int,
    val reason: String
)

enum class RefundMethod {
    CASH,
    CARD,
    STORE_CREDIT
}
