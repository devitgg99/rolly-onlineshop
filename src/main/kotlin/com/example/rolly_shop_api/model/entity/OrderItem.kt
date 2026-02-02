package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val quantity: Int,

    // Price at time of purchase (snapshot)
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    val priceAtPurchase: BigDecimal,

    // Product name snapshot (in case product is deleted)
    @Column(name = "product_name", nullable = false)
    val productName: String
) {
    fun getSubtotal(): BigDecimal = priceAtPurchase.multiply(BigDecimal(quantity))
}

