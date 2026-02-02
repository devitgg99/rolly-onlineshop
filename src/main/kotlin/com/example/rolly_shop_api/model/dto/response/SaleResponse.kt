package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.PaymentMethod
import com.example.rolly_shop_api.model.entity.Sale
import com.example.rolly_shop_api.model.entity.SaleItem
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Full sale response with all details
 */
data class SaleResponse(
    val id: UUID,
    val customerName: String?,
    val customerPhone: String?,
    val items: List<SaleItemResponse>,
    val totalAmount: BigDecimal,      // What customer paid
    val totalCost: BigDecimal,        // What shop paid for products
    val discountAmount: BigDecimal,
    val profit: BigDecimal,           // Net profit
    val profitMargin: Double,         // Profit percentage
    val paymentMethod: PaymentMethod,
    val soldBy: String?,              // Admin name
    val notes: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(sale: Sale) = SaleResponse(
            id = sale.id!!,
            customerName = sale.customerName,
            customerPhone = sale.customerPhone,
            items = sale.items.map { SaleItemResponse.from(it) },
            totalAmount = sale.totalAmount,
            totalCost = sale.totalCost,
            discountAmount = sale.discountAmount,
            profit = sale.profit,
            profitMargin = if (sale.totalAmount > BigDecimal.ZERO) {
                sale.profit.divide(sale.totalAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100)).toDouble()
            } else 0.0,
            paymentMethod = sale.paymentMethod,
            soldBy = sale.soldBy?.fullName,
            notes = sale.notes,
            createdAt = sale.createdAt
        )
    }
}

/**
 * Sale item details
 */
data class SaleItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val unitCost: BigDecimal,
    val subtotal: BigDecimal,
    val profit: BigDecimal
) {
    companion object {
        fun from(item: SaleItem) = SaleItemResponse(
            id = item.id!!,
            productId = item.product.id!!,
            productName = item.product.name,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            unitCost = item.unitCost,
            subtotal = item.subtotal,
            profit = item.profit
        )
    }
}

/**
 * Simple sale for list view
 */
data class SaleSimpleResponse(
    val id: UUID,
    val customerName: String?,
    val itemCount: Int,
    val totalAmount: BigDecimal,
    val profit: BigDecimal,
    val paymentMethod: PaymentMethod,
    val createdAt: Instant
) {
    companion object {
        fun from(sale: Sale) = SaleSimpleResponse(
            id = sale.id!!,
            customerName = sale.customerName,
            itemCount = sale.items.size,
            totalAmount = sale.totalAmount,
            profit = sale.profit,
            paymentMethod = sale.paymentMethod,
            createdAt = sale.createdAt
        )
    }
}

/**
 * Sales summary/dashboard
 */
data class SalesSummaryResponse(
    val totalSales: Long,             // Number of sales
    val totalRevenue: BigDecimal,     // Total money received
    val totalCost: BigDecimal,        // Total cost of goods sold
    val totalProfit: BigDecimal,      // Net profit
    val profitMargin: Double,         // Profit percentage
    val periodStart: Instant,
    val periodEnd: Instant
)
