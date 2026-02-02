package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.OrderRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.OrderResponse
import com.example.rolly_shop_api.model.dto.response.OrderSimpleResponse
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.entity.OrderStatus
import com.example.rolly_shop_api.model.entity.PaymentStatus
import com.example.rolly_shop_api.service.CurrentUserService
import com.example.rolly_shop_api.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management for users and admins")
class OrderController(
    private val orderService: OrderService,
    private val currentUserService: CurrentUserService
) {
    // ==================== USER ENDPOINTS ====================

    @PostMapping
    @Operation(
        summary = "Create order from cart",
        description = "👤 USER - Checkout: creates order from current cart items"
    )
    fun createOrder(@Valid @RequestBody request: OrderRequest): BaseResponse<OrderResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(orderService.createOrder(userId, request), "Order placed successfully")
    }

    @GetMapping
    @Operation(
        summary = "Get my orders",
        description = "👤 USER - Get all orders for logged-in user"
    )
    fun getMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<OrderSimpleResponse>> {
        val userId = currentUserService.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(orderService.getUserOrders(userId, pageable), "Orders retrieved")
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order details",
        description = "👤 USER - Get full order details with items"
    )
    fun getOrderById(@PathVariable id: UUID): BaseResponse<OrderResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(orderService.getOrderById(userId, id), "Order found")
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "👤 USER - Cancel order (only PENDING or CONFIRMED orders)"
    )
    fun cancelOrder(@PathVariable id: UUID): BaseResponse<OrderResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(orderService.cancelOrder(userId, id), "Order cancelled")
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all orders",
        description = "🔒 ADMIN ONLY - Get all orders in system"
    )
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<OrderSimpleResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(orderService.getAllOrders(pageable), "All orders")
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get orders by status",
        description = "🔒 ADMIN ONLY - Filter orders by status (PENDING, CONFIRMED, SHIPPED, etc.)"
    )
    fun getOrdersByStatus(
        @PathVariable status: OrderStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<OrderSimpleResponse>> {
        val pageable = PageRequest.of(page, size)
        return BaseResponse.success(orderService.getOrdersByStatus(status, pageable), "Orders by status")
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get any order details",
        description = "🔒 ADMIN ONLY - View any order details"
    )
    fun getOrderByIdAdmin(@PathVariable id: UUID): BaseResponse<OrderResponse> =
        BaseResponse.success(orderService.getOrderByIdAdmin(id), "Order found")

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update order status",
        description = "🔒 ADMIN ONLY - Update order status (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED)"
    )
    fun updateOrderStatus(
        @PathVariable id: UUID,
        @Parameter(description = "New status") @RequestParam status: OrderStatus
    ): BaseResponse<OrderResponse> =
        BaseResponse.success(orderService.updateOrderStatus(id, status), "Order status updated")

    @PatchMapping("/admin/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update payment status",
        description = "🔒 ADMIN ONLY - Update payment status (PENDING, PAID, FAILED, REFUNDED)"
    )
    fun updatePaymentStatus(
        @PathVariable id: UUID,
        @Parameter(description = "New payment status") @RequestParam status: PaymentStatus
    ): BaseResponse<OrderResponse> =
        BaseResponse.success(orderService.updatePaymentStatus(id, status), "Payment status updated")
}
