package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Walk-in Sale / POS - For admin to record in-store purchases
 * Separate from Order (which is for online customers with accounts)
 */
@Entity
@Table(name = "sales")
data class Sale(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    // Optional customer name (for receipt/record)
    @Column(name = "customer_name")
    val customerName: String? = null,

    // Optional customer phone (for loyalty/contact)
    @Column(name = "customer_phone")
    val customerPhone: String? = null,

    // Total selling price (what customer paid)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    val totalAmount: BigDecimal,

    // Total cost price (what shop paid for products)
    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    val totalCost: BigDecimal,

    // Profit = totalAmount - totalCost
    @Column(nullable = false, precision = 12, scale = 2)
    val profit: BigDecimal,

    // Discount given to customer
    @Column(name = "discount_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    // Admin who made the sale
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sold_by")
    val soldBy: User? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "sale", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableList<SaleItem> = mutableListOf()
)
