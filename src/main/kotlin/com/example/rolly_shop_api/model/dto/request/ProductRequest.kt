package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.*

data class ProductRequest(
    @field:NotBlank(message = "Product name is required")
    val name: String,

    val description: String? = null,

    val barcode: String? = null,

    @field:NotNull(message = "Cost price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Cost price must be greater than 0")
    val costPrice: BigDecimal,

    @field:NotNull(message = "Selling price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    val price: BigDecimal,

    @field:Min(value = 0, message = "Discount must be between 0 and 100")
    @field:Max(value = 100, message = "Discount must be between 0 and 100")
    val discountPercent: Int = 0,

    @field:Min(value = 0, message = "Stock quantity cannot be negative")
    val stockQuantity: Int = 0,

    val imageUrl: String? = null,
    val brandId: UUID? = null,
    val categoryId: UUID? = null
)
