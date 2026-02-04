package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.NotBlank

data class ProductImageRequest(
    @field:NotBlank(message = "Image URL is required")
    val url: String,
    
    val isPrimary: Boolean = false,
    
    val displayOrder: Int? = null
)

data class ImageReorderRequest(
    val imageOrders: List<ImageOrderItem>
)

data class ImageOrderItem(
    val imageId: String,
    val displayOrder: Int
)
