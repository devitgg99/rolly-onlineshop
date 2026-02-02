package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.NotBlank
import java.util.*

data class CategoryRequest(
    @field:NotBlank(message = "Category name is required")
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val parentId: UUID? = null
)

