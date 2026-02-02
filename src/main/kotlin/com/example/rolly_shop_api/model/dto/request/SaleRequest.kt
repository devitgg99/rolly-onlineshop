package com.example.rolly_shop_api.model.dto.request

import com.example.rolly_shop_api.model.entity.PaymentMethod
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal
import java.util.*

/**
 * Request to create a walk-in sale
 */
data class SaleRequest(
    // Optional customer info
    val customerName: String? = null,
    val customerPhone: String? = null,

    // Items being purchased
    @field:NotEmpty(message = "At least one item is required")
    val items: List<SaleItemRequest>,

    // Optional discount
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    // Payment method
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    // Optional notes
    val notes: String? = null
)

data class SaleItemRequest(
    val productId: UUID,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)
