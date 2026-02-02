package com.example.rolly_shop_api.model.dto.response

import java.math.BigDecimal

/**
 * Inventory dashboard statistics
 */
data class InventoryStatsResponse(
    val totalProducts: Long,           // Total number of products
    val totalValue: BigDecimal,        // Inventory worth (cost price × stock)
    val totalPotentialProfit: BigDecimal, // Potential earnings (selling - cost) × stock
    val lowStockCount: Long,           // Products needing restock
    val lowStockThreshold: Int         // Threshold used for low stock
)
