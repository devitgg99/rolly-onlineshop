package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.ProductImage
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProductImageRepository : JpaRepository<ProductImage, UUID> {
    fun findByProductIdOrderBySortOrder(productId: UUID): List<ProductImage>
    fun findByProductIdAndIsPrimaryTrue(productId: UUID): ProductImage?
    fun deleteByProductId(productId: UUID)
}

