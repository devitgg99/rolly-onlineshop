package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.ProductImage
import java.time.Instant
import java.util.*

data class ProductImageResponse(
    val id: String,
    val productId: String,
    val url: String,
    val isPrimary: Boolean,
    val displayOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(image: ProductImage): ProductImageResponse {
            return ProductImageResponse(
                id = image.id.toString(),
                productId = image.product.id.toString(),
                url = image.imageUrl,
                isPrimary = image.isPrimary,
                displayOrder = image.sortOrder,
                createdAt = image.createdAt,
                updatedAt = image.updatedAt
            )
        }
    }
}

data class SetPrimaryImageResponse(
    val productId: String,
    val previousPrimaryImageId: String?,
    val newPrimaryImageId: String
)

data class ImageReorderResponse(
    val productId: String,
    val updatedCount: Int
)

data class DeleteImageResponse(
    val deletedImageId: String
)
