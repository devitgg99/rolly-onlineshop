package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByUserUserId(userId: UUID): List<Cart>
    fun findByUserUserIdAndProductId(userId: UUID, productId: UUID): Cart?
    fun deleteByUserUserId(userId: UUID)
    fun deleteByUserUserIdAndProductId(userId: UUID, productId: UUID)
}

