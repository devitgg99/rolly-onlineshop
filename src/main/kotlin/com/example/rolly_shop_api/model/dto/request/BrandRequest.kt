package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.NotBlank

data class BrandRequest(
    @field:NotBlank(message = "Brand name is required")
    val name: String,
    val logoUrl: String? = null,
    val description: String? = null
)

