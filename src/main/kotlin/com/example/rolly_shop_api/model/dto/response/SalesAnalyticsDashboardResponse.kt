package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.PaymentMethod
import java.math.BigDecimal

/**
 * Comprehensive sales analytics dashboard response
 */
data class SalesAnalyticsDashboardResponse(
    val totalSales: Long,
    val totalRevenue: BigDecimal,
    val totalProfit: BigDecimal,
    val avgOrderValue: BigDecimal,
    val salesByDay: List<SalesByPeriodResponse>,
    val salesByPaymentMethod: Map<PaymentMethod, PaymentMethodSalesResponse>,
    val salesByHour: List<SalesByHourResponse>,
    val topCustomers: List<TopCustomerResponse>,
    val profitMarginTrend: List<ProfitMarginTrendResponse>
)

/**
 * Sales grouped by period (day/week/month)
 */
data class SalesByPeriodResponse(
    val date: String,
    val sales: Long,
    val revenue: BigDecimal,
    val profit: BigDecimal
)

/**
 * Sales by payment method
 */
data class PaymentMethodSalesResponse(
    val count: Long,
    val revenue: BigDecimal
)

/**
 * Sales by hour of day
 */
data class SalesByHourResponse(
    val hour: Int,
    val sales: Long,
    val revenue: BigDecimal
)

/**
 * Top customer information
 */
data class TopCustomerResponse(
    val name: String,
    val phone: String?,
    val totalSpent: BigDecimal,
    val orderCount: Long
)

/**
 * Profit margin trend over time
 */
data class ProfitMarginTrendResponse(
    val date: String,
    val margin: Double
)
