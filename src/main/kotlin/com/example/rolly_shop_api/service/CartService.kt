package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.CartRequest
import com.example.rolly_shop_api.model.dto.request.CartUpdateRequest
import com.example.rolly_shop_api.model.dto.response.CartResponse
import java.util.*

interface CartService {
    fun addToCart(userId: UUID, request: CartRequest): CartResponse
    fun updateQuantity(userId: UUID, productId: UUID, request: CartUpdateRequest): CartResponse
    fun removeFromCart(userId: UUID, productId: UUID): CartResponse
    fun getCart(userId: UUID): CartResponse
    fun clearCart(userId: UUID)
}

