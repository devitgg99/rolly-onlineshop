package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.*

data class ReviewRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: UUID,

    @field:Min(value = 1, message = "Rating must be between 1 and 5")
    @field:Max(value = 5, message = "Rating must be between 1 and 5")
    val rating: Int,

    val comment: String? = null
)

