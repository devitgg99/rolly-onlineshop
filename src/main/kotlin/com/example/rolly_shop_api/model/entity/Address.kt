package com.example.rolly_shop_api.model.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "addresses")
data class Address(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "full_name", nullable = false)
    val fullName: String,

    @Column(name = "phone_number", nullable = false)
    val phoneNumber: String,

    @Column(name = "address_line", nullable = false)
    val addressLine: String,

    @Column(nullable = false)
    val city: String,

    val province: String? = null,

    @Column(name = "postal_code")
    val postalCode: String? = null,

    @Column(name = "is_default")
    var isDefault: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

