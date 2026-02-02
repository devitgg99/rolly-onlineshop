package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Brand
import java.time.Instant
import java.util.*

data class BrandResponse(
    val id: UUID,
    val name: String,
    val logoUrl: String?,
    val description: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(brand: Brand) = BrandResponse(
            id = brand.id!!,
            name = brand.name,
            logoUrl = brand.logoUrl,
            description = brand.description,
            createdAt = brand.createdAt
        )
    }
}
