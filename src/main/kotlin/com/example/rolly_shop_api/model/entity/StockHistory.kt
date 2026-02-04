package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

enum class AdjustmentType {
    SALE, RESTOCK, DAMAGE, MANUAL, RETURN, CORRECTION
}

enum class ReferenceType {
    SALE, PURCHASE_ORDER, ADJUSTMENT
}

@Entity
@Table(name = "stock_history")
data class StockHistory(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "previous_stock", nullable = false)
    val previousStock: Int,

    @Column(name = "new_stock", nullable = false)
    val newStock: Int,

    @Column(name = "adjustment", nullable = false)
    val adjustment: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    val adjustmentType: AdjustmentType,

    @Column(name = "reason", columnDefinition = "TEXT")
    val reason: String? = null,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    val referenceType: ReferenceType? = null,

    @Column(name = "updated_by", nullable = false)
    val updatedBy: String,

    @Column(name = "updated_by_name", nullable = false)
    val updatedByName: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
