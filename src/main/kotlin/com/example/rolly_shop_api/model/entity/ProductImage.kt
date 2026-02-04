package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "product_images")
data class ProductImage(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Column(name = "is_primary")
    var isPrimary: Boolean = false,

    @Column(name = "sort_order")
    var sortOrder: Int = 0,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

