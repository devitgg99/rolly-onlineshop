package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Product
import java.math.BigDecimal
import java.time.Instant
import java.util.*

// ==================== PUBLIC RESPONSES (Customer View) ====================

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val barcode: String?,
    val price: BigDecimal,
    val discountPercent: Int,
    val discountedPrice: BigDecimal,
    val stockQuantity: Int,
    val imageUrl: String?,
    val brand: BrandResponse?,
    val category: CategoryResponse?,
    val averageRating: Double?,
    val createdAt: Instant
) {
    companion object {
        fun from(product: Product, averageRating: Double? = null) = ProductResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            barcode = product.barcode,
            price = product.price,
            discountPercent = product.discountPercent,
            discountedPrice = product.getDiscountedPrice(),
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            brand = product.brand?.let { BrandResponse.from(it) },
            category = product.category?.let { CategoryResponse.from(it) },
            averageRating = averageRating,
            createdAt = product.createdAt
        )
    }
}

// Simple version for lists (Customer View)
data class ProductSimpleResponse(
    val id: UUID,
    val name: String,
    val barcode: String?,
    val price: BigDecimal,
    val discountPercent: Int,
    val discountedPrice: BigDecimal,
    val stockQuantity: Int,
    val imageUrl: String?,
    val brandName: String?,
    val categoryName: String?
) {
    companion object {
        fun from(product: Product) = ProductSimpleResponse(
            id = product.id!!,
            name = product.name,
            barcode = product.barcode,
            price = product.price,
            discountPercent = product.discountPercent,
            discountedPrice = product.getDiscountedPrice(),
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            brandName = product.brand?.name,
            categoryName = product.category?.name
        )
    }
}

// ==================== ADMIN RESPONSES (includes cost price & profit) ====================

data class ProductAdminResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val barcode: String?,
    val costPrice: BigDecimal,       // Admin only: what shop paid
    val price: BigDecimal,           // Selling price
    val discountPercent: Int,
    val discountedPrice: BigDecimal,
    val profit: BigDecimal,          // Admin only: selling price - cost price
    val stockQuantity: Int,
    val imageUrl: String?,
    val brand: BrandResponse?,
    val category: CategoryResponse?,
    val averageRating: Double?,
    val createdAt: Instant
) {
    companion object {
        fun from(product: Product, averageRating: Double? = null) = ProductAdminResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            barcode = product.barcode,
            costPrice = product.costPrice,
            price = product.price,
            discountPercent = product.discountPercent,
            discountedPrice = product.getDiscountedPrice(),
            profit = product.getProfit(),
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            brand = product.brand?.let { BrandResponse.from(it) },
            category = product.category?.let { CategoryResponse.from(it) },
            averageRating = averageRating,
            createdAt = product.createdAt
        )
    }
}

// Simple version for admin lists
data class ProductAdminSimpleResponse(
    val id: UUID,
    val name: String,
    val barcode: String?,
    val costPrice: BigDecimal,       // Admin only
    val price: BigDecimal,
    val discountPercent: Int,
    val discountedPrice: BigDecimal,
    val profit: BigDecimal,          // Admin only
    val stockQuantity: Int,
    val imageUrl: String?,
    val brandName: String?,
    val categoryName: String?
) {
    companion object {
        fun from(product: Product) = ProductAdminSimpleResponse(
            id = product.id!!,
            name = product.name,
            barcode = product.barcode,
            costPrice = product.costPrice,
            price = product.price,
            discountPercent = product.discountPercent,
            discountedPrice = product.getDiscountedPrice(),
            profit = product.getProfit(),
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            brandName = product.brand?.name,
            categoryName = product.category?.name
        )
    }
}
