package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
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
    val isPrimary: Boolean = false,

    @Column(name = "sort_order")
    val sortOrder: Int = 0
)

