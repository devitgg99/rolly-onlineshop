package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Product
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Admin product inventory table view - shows all product info with sales data
 */
data class ProductInventoryResponse(
    val id: UUID,
    val name: String,
    val barcode: String?,
    val categoryName: String?,
    val brandName: String?,
    
    // Pricing
    val costPrice: BigDecimal,          // What you pay (cost)
    val price: BigDecimal,              // Original selling price
    val discountPercent: Int,           // Discount %
    val sellingPrice: BigDecimal,       // Final selling price after discount
    val profit: BigDecimal,             // Profit per unit (sellingPrice - costPrice)
    
    // Stock
    val stockQuantity: Int,
    val stockValue: BigDecimal,         // costPrice * stockQuantity
    
    // Sales data
    val totalSold: Int,                 // Total quantity sold
    val totalRevenue: BigDecimal,       // Total money from sales
    val totalProfit: BigDecimal,        // Total profit from sales
    
    // Metadata
    val imageUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
) {
    companion object {
        fun from(
            product: Product,
            totalSold: Int,
            totalRevenue: BigDecimal,
            totalProfit: BigDecimal
        ): ProductInventoryResponse {
            val sellingPrice = product.getDiscountedPrice()
            val unitProfit = sellingPrice.subtract(product.costPrice)
            
            return ProductInventoryResponse(
                id = product.id!!,
                name = product.name,
                barcode = product.barcode,
                categoryName = product.category?.name,
                brandName = product.brand?.name,
                costPrice = product.costPrice,
                price = product.price,
                discountPercent = product.discountPercent,
                sellingPrice = sellingPrice,
                profit = unitProfit,
                stockQuantity = product.stockQuantity,
                stockValue = product.costPrice.multiply(BigDecimal(product.stockQuantity)),
                totalSold = totalSold,
                totalRevenue = totalRevenue,
                totalProfit = totalProfit,
                imageUrl = product.imageUrl,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}
