package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.CartRequest
import com.example.rolly_shop_api.model.dto.request.CartUpdateRequest
import com.example.rolly_shop_api.model.dto.response.CartResponse
import com.example.rolly_shop_api.model.entity.Cart
import com.example.rolly_shop_api.repository.CartRepository
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.UserRepository
import com.example.rolly_shop_api.service.CartService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class CartServiceImplement(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) : CartService {

    @Transactional
    override fun addToCart(userId: UUID, request: CartRequest): CartResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        val product = productRepository.findById(request.productId)
            .orElseThrow { NoSuchElementException("Product not found") }

        if (product.stockQuantity < request.quantity) {
            throw IllegalArgumentException("Not enough stock available")
        }

        // Check if product already in cart
        val existingCart = cartRepository.findByUserUserIdAndProductId(userId, request.productId)
        if (existingCart != null) {
            existingCart.quantity += request.quantity
            existingCart.updatedAt = Instant.now()
            cartRepository.save(existingCart)
        } else {
            val cart = Cart(user = user, product = product, quantity = request.quantity)
            cartRepository.save(cart)
        }

        return getCart(userId)
    }

    @Transactional
    override fun updateQuantity(userId: UUID, productId: UUID, request: CartUpdateRequest): CartResponse {
        val cart = cartRepository.findByUserUserIdAndProductId(userId, productId)
            ?: throw NoSuchElementException("Product not in cart")

        if (cart.product.stockQuantity < request.quantity) {
            throw IllegalArgumentException("Not enough stock available")
        }

        cart.quantity = request.quantity
        cart.updatedAt = Instant.now()
        cartRepository.save(cart)

        return getCart(userId)
    }

    @Transactional
    override fun removeFromCart(userId: UUID, productId: UUID): CartResponse {
        cartRepository.deleteByUserUserIdAndProductId(userId, productId)
        return getCart(userId)
    }

    override fun getCart(userId: UUID): CartResponse {
        val carts = cartRepository.findByUserUserId(userId)
        return CartResponse.from(carts)
    }

    @Transactional
    override fun clearCart(userId: UUID) {
        cartRepository.deleteByUserUserId(userId)
    }
}

