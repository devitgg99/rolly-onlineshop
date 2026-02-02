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

