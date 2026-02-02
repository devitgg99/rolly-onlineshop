package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val priceAtPurchase: BigDecimal,
    val subtotal: BigDecimal
) {
    companion object {
        fun from(item: OrderItem) = OrderItemResponse(
            id = item.id!!,
            productId = item.product.id!!,
            productName = item.productName,
            quantity = item.quantity,
            priceAtPurchase = item.priceAtPurchase,
            subtotal = item.getSubtotal()
        )
    }
}

data class OrderResponse(
    val id: UUID,
    val address: AddressResponse,
    val items: List<OrderItemResponse>,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val shippingFee: BigDecimal,
    val status: OrderStatus,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val notes: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            id = order.id!!,
            address = AddressResponse.from(order.address),
            items = order.items.map { OrderItemResponse.from(it) },
            totalAmount = order.totalAmount,
            discountAmount = order.discountAmount,
            shippingFee = order.shippingFee,
            status = order.status,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            notes = order.notes,
            createdAt = order.createdAt
        )
    }
}

// Simple version for list view
data class OrderSimpleResponse(
    val id: UUID,
    val totalAmount: BigDecimal,
    val itemCount: Int,
    val status: OrderStatus,
    val paymentStatus: PaymentStatus,
    val createdAt: Instant
) {
    companion object {
        fun from(order: Order) = OrderSimpleResponse(
            id = order.id!!,
            totalAmount = order.totalAmount,
            itemCount = order.items.size,
            status = order.status,
            paymentStatus = order.paymentStatus,
            createdAt = order.createdAt
        )
    }
}

