package com.example.rolly_shop_api.model.dto.request


data class UserRequest(
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val email: String,
)