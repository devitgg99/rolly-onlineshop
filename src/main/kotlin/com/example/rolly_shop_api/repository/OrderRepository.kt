package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.Order
import com.example.rolly_shop_api.model.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserUserId(userId: UUID, pageable: Pageable): Page<Order>
    fun findByUserUserIdAndStatus(userId: UUID, status: OrderStatus, pageable: Pageable): Page<Order>
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>
    fun countByUserUserId(userId: UUID): Long
}

