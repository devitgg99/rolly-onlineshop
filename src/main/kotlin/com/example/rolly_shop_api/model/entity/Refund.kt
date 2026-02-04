package com.example.rolly_shop_api.model.entity

import com.example.rolly_shop_api.model.dto.request.RefundMethod
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Refund record for a sale
 */
@Entity
@Table(name = "refunds")
data class Refund(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    // Total amount being refunded
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    val refundAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false)
    val refundMethod: RefundMethod = RefundMethod.CASH,

    // Admin who processed the refund
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    val processedBy: User? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "refund", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableList<RefundItem> = mutableListOf()
)

/**
 * Individual item in a refund
 */
@Entity
@Table(name = "refund_items")
data class RefundItem(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    val refund: Refund,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val quantity: Int,

    // Price at time of original sale
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,

    // Subtotal = unitPrice * quantity
    @Column(nullable = false, precision = 10, scale = 2)
    val subtotal: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val reason: String?
)
