package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.*

data class CartRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1
)

data class CartUpdateRequest(
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)

