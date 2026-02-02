package com.example.rolly_shop_api.model.dto.request

import com.example.rolly_shop_api.model.entity.PaymentMethod
import jakarta.validation.constraints.NotNull
import java.util.*

data class OrderRequest(
    @field:NotNull(message = "Address ID is required")
    val addressId: UUID,

    @field:NotNull(message = "Payment method is required")
    val paymentMethod: PaymentMethod,

    val notes: String? = null
)

