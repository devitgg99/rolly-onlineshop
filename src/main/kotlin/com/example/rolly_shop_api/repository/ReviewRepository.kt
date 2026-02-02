package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ReviewRepository : JpaRepository<Review, UUID> {
    fun findByProductId(productId: UUID, pageable: Pageable): Page<Review>
    fun findByUserUserId(userId: UUID, pageable: Pageable): Page<Review>
    fun findByUserUserIdAndProductId(userId: UUID, productId: UUID): Review?
    fun existsByUserUserIdAndProductId(userId: UUID, productId: UUID): Boolean

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    fun getAverageRating(productId: UUID): Double?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    fun countByProductId(productId: UUID): Long
}

