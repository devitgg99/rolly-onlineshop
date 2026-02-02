package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Category
import java.time.Instant
import java.util.*

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val parentId: UUID?,
    val createdAt: Instant
) {
    companion object {
        fun from(category: Category) = CategoryResponse(
            id = category.id!!,
            name = category.name,
            description = category.description,
            imageUrl = category.imageUrl,
            parentId = category.parent?.id,
            createdAt = category.createdAt
        )
    }
}
