package com.example.rolly_shop_api.model.dto.request

import jakarta.validation.constraints.NotBlank

data class AddressRequest(
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @field:NotBlank(message = "Phone number is required")
    val phoneNumber: String,

    @field:NotBlank(message = "Address is required")
    val addressLine: String,

    @field:NotBlank(message = "City is required")
    val city: String,

    val province: String? = null,
    val postalCode: String? = null,
    val isDefault: Boolean = false
)

