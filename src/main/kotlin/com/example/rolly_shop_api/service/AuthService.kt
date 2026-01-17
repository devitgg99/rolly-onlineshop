package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.UserLogin
import com.example.rolly_shop_api.model.dto.request.UserRequest


interface AuthService {
    fun register(userRequest: UserRequest)
    fun login(userRequest: UserLogin): String
}