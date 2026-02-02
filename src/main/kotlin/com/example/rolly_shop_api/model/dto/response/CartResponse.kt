package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Cart
import java.math.BigDecimal
import java.util.*

data class CartItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val productImage: String?,
    val price: BigDecimal,
    val discountedPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
) {
    companion object {
        fun from(cart: Cart) = CartItemResponse(
            id = cart.id!!,
            productId = cart.product.id!!,
            productName = cart.product.name,
            productImage = cart.product.imageUrl,
            price = cart.product.price,
            discountedPrice = cart.product.getDiscountedPrice(),
            quantity = cart.quantity,
            subtotal = cart.product.getDiscountedPrice().multiply(BigDecimal(cart.quantity))
        )
    }
}

data class CartResponse(
    val items: List<CartItemResponse>,
    val totalItems: Int,
    val totalAmount: BigDecimal
) {
    companion object {
        fun from(carts: List<Cart>): CartResponse {
            val items = carts.map { CartItemResponse.from(it) }
            return CartResponse(
                items = items,
                totalItems = items.sumOf { it.quantity },
                totalAmount = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal) }
            )
        }
    }
}

