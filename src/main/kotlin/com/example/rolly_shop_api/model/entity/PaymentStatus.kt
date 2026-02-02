package com.example.rolly_shop_api.model.entity

enum class PaymentStatus {
    PENDING,   // Awaiting payment
    PAID,      // Payment received
    FAILED,    // Payment failed
    REFUNDED   // Payment refunded
}

