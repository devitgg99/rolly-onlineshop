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

// Variant information (used in grouped views)
data class ProductVariantInfo(
    val id: UUID,
    val variantCode: String?,
    val variantColor: String?,
    val variantSize: String?,
    val stockQuantity: Int,
    val price: BigDecimal,
    val discountedPrice: BigDecimal,
    val costPrice: BigDecimal,
    val profit: BigDecimal,
    val barcode: String?
) {
    companion object {
        fun from(product: Product) = ProductVariantInfo(
            id = product.id!!,
            variantCode = product.variantCode,
            variantColor = product.variantColor,
            variantSize = product.variantSize,
            stockQuantity = product.stockQuantity,
            price = product.price,
            discountedPrice = product.getDiscountedPrice(),
            costPrice = product.costPrice,
            profit = product.getProfit(),
            barcode = product.barcode
        )
    }

    // Display name for variant
    fun getDisplayName(): String {
        val parts = mutableListOf<String>()
        variantCode?.let { parts.add("Code $it") }
        variantColor?.let { parts.add(it) }
        variantSize?.let { parts.add(it) }
        return if (parts.isEmpty()) "Default" else parts.joinToString(" - ")
    }
}

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
    val stockQuantity: Int,          // Own stock (0 if parent with variants)
    val imageUrl: String?,
    val brand: BrandResponse?,
    val category: CategoryResponse?,
    val averageRating: Double?,
    val createdAt: Instant,

    // ==================== VARIANT FIELDS ====================
    val parentProductId: UUID? = null,
    val isVariant: Boolean = false,
    val variantCode: String? = null,
    val variantColor: String? = null,
    val variantSize: String? = null,
    val variants: List<ProductVariantInfo>? = null,  // If parent, list of variants
    val totalVariantStock: Int? = null               // Total stock across all variants
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
            createdAt = product.createdAt,
            parentProductId = product.parentProductId,
            isVariant = product.isVariant,
            variantCode = product.variantCode,
            variantColor = product.variantColor,
            variantSize = product.variantSize,
            variants = null,  // Populated separately if needed
            totalVariantStock = null  // Calculated separately
        )
    }
    
    // Calculate total stock (includes variants if parent)
    fun getTotalStock(): Int {
        return if (variants != null && variants.isNotEmpty()) {
            variants.sumOf { it.stockQuantity }
        } else {
            stockQuantity
        }
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
    val categoryName: String?,

    // ==================== VARIANT FIELDS ====================
    val parentProductId: UUID? = null,
    val isVariant: Boolean = false,
    val variantCode: String? = null,
    val variantColor: String? = null,
    val variantSize: String? = null,
    val hasVariants: Boolean = false  // True if this product has variants
) {
    companion object {
        fun from(product: Product, hasVariants: Boolean = false) = ProductAdminSimpleResponse(
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
            categoryName = product.category?.name,
            parentProductId = product.parentProductId,
            isVariant = product.isVariant,
            variantCode = product.variantCode,
            variantColor = product.variantColor,
            variantSize = product.variantSize,
            hasVariants = hasVariants
        )
    }

    // Get display name with variant info
    fun getFullName(): String {
        val parts = mutableListOf(name)
        variantCode?.let { parts.add("Code $it") }
        variantColor?.let { parts.add(it) }
        variantSize?.let { parts.add(it) }
        return parts.joinToString(" - ")
    }
}
