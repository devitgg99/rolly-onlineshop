package com.example.rolly_shop_api.model.entity

enum class OrderStatus {
    PENDING,      // Order placed, awaiting confirmation
    CONFIRMED,    // Order confirmed by admin
    PROCESSING,   // Being prepared
    SHIPPED,      // Out for delivery
    DELIVERED,    // Delivered to customer
    COMPLETED,    // Customer confirmed receipt
    CANCELLED,    // Order cancelled
    RETURNED      // Order returned
}

