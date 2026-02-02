package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.CartRequest
import com.example.rolly_shop_api.model.dto.request.CartUpdateRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.CartResponse
import com.example.rolly_shop_api.service.CartService
import com.example.rolly_shop_api.service.CurrentUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart", description = "Cart management for users")
class CartController(
    private val cartService: CartService,
    private val currentUserService: CurrentUserService
) {
    // ==================== USER ENDPOINTS (Authenticated) ====================

    @GetMapping
    @Operation(
        summary = "Get my cart",
        description = "👤 USER - Get current user's shopping cart with items and totals"
    )
    fun getCart(): BaseResponse<CartResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(cartService.getCart(userId), "Cart retrieved")
    }

    @PostMapping
    @Operation(
        summary = "Add to cart",
        description = "👤 USER - Add product to cart. If already in cart, quantity is added."
    )
    fun addToCart(@Valid @RequestBody request: CartRequest): BaseResponse<CartResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(cartService.addToCart(userId, request), "Added to cart")
    }

    @PutMapping("/{productId}")
    @Operation(
        summary = "Update quantity",
        description = "👤 USER - Update quantity of product in cart"
    )
    fun updateQuantity(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: CartUpdateRequest
    ): BaseResponse<CartResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(cartService.updateQuantity(userId, productId, request), "Cart updated")
    }

    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Remove from cart",
        description = "👤 USER - Remove product from cart"
    )
    fun removeFromCart(@PathVariable productId: UUID): BaseResponse<CartResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(cartService.removeFromCart(userId, productId), "Removed from cart")
    }

    @DeleteMapping
    @Operation(
        summary = "Clear cart",
        description = "👤 USER - Remove all items from cart"
    )
    fun clearCart(): BaseResponse<Unit> {
        val userId = currentUserService.getCurrentUserId()
        cartService.clearCart(userId)
        return BaseResponse.ok("Cart cleared")
    }
}
