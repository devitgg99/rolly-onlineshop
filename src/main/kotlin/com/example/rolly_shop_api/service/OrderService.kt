package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.OrderRequest
import com.example.rolly_shop_api.model.dto.response.OrderResponse
import com.example.rolly_shop_api.model.dto.response.OrderSimpleResponse
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.entity.OrderStatus
import com.example.rolly_shop_api.model.entity.PaymentStatus
import org.springframework.data.domain.Pageable
import java.util.*

interface OrderService {
    // User operations
    fun createOrder(userId: UUID, request: OrderRequest): OrderResponse
    fun getOrderById(userId: UUID, orderId: UUID): OrderResponse
    fun getUserOrders(userId: UUID, pageable: Pageable): PageResponse<OrderSimpleResponse>
    fun cancelOrder(userId: UUID, orderId: UUID): OrderResponse

    // Admin operations
    fun getAllOrders(pageable: Pageable): PageResponse<OrderSimpleResponse>
    fun getOrdersByStatus(status: OrderStatus, pageable: Pageable): PageResponse<OrderSimpleResponse>
    fun updateOrderStatus(orderId: UUID, status: OrderStatus): OrderResponse
    fun updatePaymentStatus(orderId: UUID, status: PaymentStatus): OrderResponse
    fun getOrderByIdAdmin(orderId: UUID): OrderResponse
}

