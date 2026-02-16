package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    // Barcode (unique identifier for scanning)
    @Column(unique = true)
    val barcode: String? = null,

    // Cost price (what shop paid to buy) - Admin only
    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    val costPrice: BigDecimal,

    // Selling price (what customer pays)
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @Column(name = "discount_percent")
    val discountPercent: Int = 0,

    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int = 0,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    val brand: Brand? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    // ==================== VARIANT FIELDS ====================
    // Parent-child relationship for product variants
    @Column(name = "parent_product_id")
    val parentProductId: UUID? = null,

    @Column(name = "is_variant")
    val isVariant: Boolean = false,

    // Variant attributes (all optional)
    @Column(name = "variant_code", length = 50)
    val variantCode: String? = null,

    @Column(name = "variant_color", length = 50)
    val variantColor: String? = null,

    @Column(name = "variant_size", length = 50)
    val variantSize: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) {
    // Calculate discounted price
    fun getDiscountedPrice(): BigDecimal {
        if (discountPercent <= 0) return price
        val discount = price.multiply(BigDecimal(discountPercent)).divide(BigDecimal(100))
        return price.subtract(discount)
    }

    // Calculate profit (selling price - cost price)
    fun getProfit(): BigDecimal = getDiscountedPrice().subtract(costPrice)
}

