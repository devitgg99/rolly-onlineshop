package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

/**
 * Individual item in a walk-in sale
 */
@Entity
@Table(name = "sale_items")
data class SaleItem(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val quantity: Int,

    // Price at time of sale (selling price)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,

    // Cost at time of sale (for profit calculation)
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    val unitCost: BigDecimal,

    // Subtotal = unitPrice * quantity
    @Column(nullable = false, precision = 10, scale = 2)
    val subtotal: BigDecimal,

    // Item profit = (unitPrice - unitCost) * quantity
    @Column(nullable = false, precision = 10, scale = 2)
    val profit: BigDecimal
)
