package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.OrderRequest
import com.example.rolly_shop_api.model.dto.response.OrderResponse
import com.example.rolly_shop_api.model.dto.response.OrderSimpleResponse
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.entity.*
import com.example.rolly_shop_api.repository.*
import com.example.rolly_shop_api.service.CartService
import com.example.rolly_shop_api.service.OrderService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class OrderServiceImplement(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val addressRepository: AddressRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val cartService: CartService
) : OrderService {

    @Transactional
    override fun createOrder(userId: UUID, request: OrderRequest): OrderResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        val address = addressRepository.findById(request.addressId)
            .orElseThrow { NoSuchElementException("Address not found") }

        if (address.user.userId != userId) {
            throw IllegalArgumentException("Address does not belong to user")
        }

        // Get cart items
        val cartItems = cartRepository.findByUserUserId(userId)
        if (cartItems.isEmpty()) {
            throw IllegalArgumentException("Cart is empty")
        }

        // Calculate total
        var totalAmount = BigDecimal.ZERO
        val orderItems = mutableListOf<OrderItem>()

        // Validate stock and prepare order items
        for (cartItem in cartItems) {
            val product = cartItem.product
            if (product.stockQuantity < cartItem.quantity) {
                throw IllegalArgumentException("Not enough stock for ${product.name}")
            }
            val itemPrice = product.getDiscountedPrice()
            totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal(cartItem.quantity)))
        }

        // Create order
        val order = Order(
            user = user,
            address = address,
            totalAmount = totalAmount,
            paymentMethod = request.paymentMethod,
            notes = request.notes
        )
        val savedOrder = orderRepository.save(order)

        // Create order items and update stock
        for (cartItem in cartItems) {
            val product = cartItem.product
            val orderItem = OrderItem(
                order = savedOrder,
                product = product,
                quantity = cartItem.quantity,
                priceAtPurchase = product.getDiscountedPrice(),
                productName = product.name
            )
            orderItems.add(orderItemRepository.save(orderItem))

            // Reduce stock
            product.stockQuantity -= cartItem.quantity
            productRepository.save(product)
        }

        savedOrder.items.addAll(orderItems)

        // Clear cart
        cartService.clearCart(userId)

        return OrderResponse.from(savedOrder)
    }

    override fun getOrderById(userId: UUID, orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }

        if (order.user.userId != userId) {
            throw IllegalArgumentException("Order does not belong to user")
        }
        return OrderResponse.from(order)
    }

    override fun getUserOrders(userId: UUID, pageable: Pageable): PageResponse<OrderSimpleResponse> {
        val page = orderRepository.findByUserUserId(userId, pageable)
        return PageResponse.from(page) { OrderSimpleResponse.from(it) }
    }

    @Transactional
    override fun cancelOrder(userId: UUID, orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }

        if (order.user.userId != userId) {
            throw IllegalArgumentException("Order does not belong to user")
        }

        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.CONFIRMED) {
            throw IllegalArgumentException("Order cannot be cancelled at this stage")
        }

        // Restore stock
        for (item in order.items) {
            val product = item.product
            product.stockQuantity += item.quantity
            productRepository.save(product)
        }

        order.status = OrderStatus.CANCELLED
        order.updatedAt = Instant.now()
        return OrderResponse.from(orderRepository.save(order))
    }

    // Admin operations
    override fun getAllOrders(pageable: Pageable): PageResponse<OrderSimpleResponse> {
        val page = orderRepository.findAll(pageable)
        return PageResponse.from(page) { OrderSimpleResponse.from(it) }
    }

    override fun getOrdersByStatus(status: OrderStatus, pageable: Pageable): PageResponse<OrderSimpleResponse> {
        val page = orderRepository.findByStatus(status, pageable)
        return PageResponse.from(page) { OrderSimpleResponse.from(it) }
    }

    @Transactional
    override fun updateOrderStatus(orderId: UUID, status: OrderStatus): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }

        order.status = status
        order.updatedAt = Instant.now()
        return OrderResponse.from(orderRepository.save(order))
    }

    @Transactional
    override fun updatePaymentStatus(orderId: UUID, status: PaymentStatus): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }

        order.paymentStatus = status
        order.updatedAt = Instant.now()
        return OrderResponse.from(orderRepository.save(order))
    }

    override fun getOrderByIdAdmin(orderId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }
        return OrderResponse.from(order)
    }
}

