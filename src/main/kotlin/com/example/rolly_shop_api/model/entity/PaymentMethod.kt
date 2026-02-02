package com.example.rolly_shop_api.model.entity

enum class PaymentMethod {
    CASH,         // Cash (in-store payment)
    COD,          // Cash on delivery
    CARD,         // Credit/Debit card
    BANK_TRANSFER,// Bank transfer
    E_WALLET      // Digital wallet (ABA, Wing, etc.)
}

