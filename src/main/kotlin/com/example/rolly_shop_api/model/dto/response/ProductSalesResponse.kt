package com.example.rolly_shop_api.model.dto.response

import java.math.BigDecimal
import java.util.*

/**
 * Sales statistics for a single product
 */
data class ProductSalesStatsResponse(
    val productId: UUID,
    val productName: String,
    val totalQuantitySold: Int,      // Total units sold
    val totalRevenue: BigDecimal,     // Total money from sales
    val totalProfit: BigDecimal,      // Total profit
    val currentStock: Int             // Current stock remaining
)

/**
 * Top selling product entry
 */
data class TopSellingProductResponse(
    val productId: UUID,
    val productName: String,
    val totalQuantitySold: Long
)

/**
 * Overall sales analytics
 */
data class SalesAnalyticsResponse(
    val topSellingProducts: List<TopSellingProductResponse>,
    val totalProductsSold: Long,       // Total different products sold
    val totalUnitsSold: Long,          // Total quantity of all products
    val totalRevenue: BigDecimal,
    val totalProfit: BigDecimal
)
