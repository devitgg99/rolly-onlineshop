package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.entity.Product
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.SaleItemRepository
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.*

@Service
class ProductExportService(
    private val productRepository: ProductRepository,
    private val saleItemRepository: SaleItemRepository
) {

    fun exportToCSV(
        response: HttpServletResponse,
        brandId: UUID?,
        categoryId: UUID?,
        lowStock: Boolean?,
        search: String?,
        sortBy: String,
        direction: String
    ) {
        val products = getFilteredProducts(brandId, categoryId, lowStock, search, sortBy, direction)
        
        response.contentType = "text/csv; charset=utf-8"
        response.setHeader("Content-Disposition", "attachment; filename=\"products-export-${LocalDate.now()}.csv\"")
        
        val writer = OutputStreamWriter(response.outputStream, StandardCharsets.UTF_8)
        
        // Write BOM for Excel UTF-8 compatibility
        writer.write("\uFEFF")
        
        // Write header
        writer.write("\"ID\",\"Name\",\"Barcode\",\"Category\",\"Brand\",\"Cost Price\",\"Selling Price\",\"Discount %\",\"Final Price\",\"Profit per Unit\",\"Stock Quantity\",\"Stock Value\",\"Total Sold\",\"Total Revenue\",\"Total Profit\",\"Image URL\",\"Created At\",\"Updated At\"\n")
        
        // Write data
        products.forEach { product ->
            val salesData = getSalesData(product.id!!)
            writer.write(toCsvRow(product, salesData))
        }
        
        writer.flush()
    }

    fun exportToExcel(
        response: HttpServletResponse,
        brandId: UUID?,
        categoryId: UUID?,
        lowStock: Boolean?,
        search: String?,
        sortBy: String,
        direction: String
    ) {
        val products = getFilteredProducts(brandId, categoryId, lowStock, search, sortBy, direction)
        
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader("Content-Disposition", "attachment; filename=\"products-export-${LocalDate.now()}.xlsx\"")
        
        val workbook = XSSFWorkbook()
        
        // Sheet 1: Products
        createProductsSheet(workbook, products)
        
        // Sheet 2: Summary
        createSummarySheet(workbook, products)
        
        // Sheet 3: Low Stock Alert
        createLowStockSheet(workbook, products)
        
        workbook.write(response.outputStream)
        workbook.close()
    }

    private fun getFilteredProducts(
        brandId: UUID?,
        categoryId: UUID?,
        lowStock: Boolean?,
        search: String?,
        sortBy: String,
        direction: String
    ): List<Product> {
        val sort = if (direction == "asc") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }
        
        var products = productRepository.findAll(sort)
        
        // Apply filters
        if (brandId != null) {
            products = products.filter { it.brand?.id == brandId }
        }
        
        if (categoryId != null) {
            products = products.filter { it.category?.id == categoryId }
        }
        
        if (lowStock == true) {
            products = products.filter { it.stockQuantity <= 10 }
        }
        
        if (!search.isNullOrBlank()) {
            products = products.filter { 
                it.name.contains(search, ignoreCase = true) 
            }
        }
        
        return products
    }

    private fun getSalesData(productId: UUID): SalesData {
        val saleItems = saleItemRepository.findByProductId(productId)
        
        val totalSold = saleItems.sumOf { it.quantity }
        val totalRevenue = saleItems.sumOf { it.subtotal }
        
        return SalesData(
            totalSold = totalSold,
            totalRevenue = totalRevenue
        )
    }

    private fun toCsvRow(product: Product, salesData: SalesData): String {
        val finalPrice = product.getDiscountedPrice()
        val profitPerUnit = product.getProfit()
        val stockValue = product.costPrice.multiply(BigDecimal(product.stockQuantity))
        val totalProfit = profitPerUnit.multiply(BigDecimal(salesData.totalSold))
        
        return listOf(
            product.id.toString(),
            product.name,
            product.barcode ?: "",
            product.category?.name ?: "",
            product.brand?.name ?: "",
            product.costPrice.toString(),
            product.price.toString(),
            product.discountPercent.toString(),
            finalPrice.toString(),
            profitPerUnit.toString(),
            product.stockQuantity.toString(),
            stockValue.toString(),
            salesData.totalSold.toString(),
            salesData.totalRevenue.toString(),
            totalProfit.toString(),
            product.imageUrl ?: "",
            product.createdAt.toString(),
            product.updatedAt.toString()
        ).joinToString(",") { "\"$it\"" } + "\n"
    }

    private fun createProductsSheet(workbook: Workbook, products: List<Product>) {
        val sheet = workbook.createSheet("Products")
        
        // Header style
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                bold = true
            })
        }
        
        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "ID", "Name", "Barcode", "Category", "Brand", 
            "Cost Price", "Selling Price", "Discount %", "Final Price", 
            "Profit per Unit", "Stock Quantity", "Stock Value", 
            "Total Sold", "Total Revenue", "Total Profit",
            "Image URL", "Created At", "Updated At"
        )
        
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // Data rows
        products.forEachIndexed { rowIndex, product ->
            val row = sheet.createRow(rowIndex + 1)
            val salesData = getSalesData(product.id!!)
            
            val finalPrice = product.getDiscountedPrice()
            val profitPerUnit = product.getProfit()
            val stockValue = product.costPrice.multiply(BigDecimal(product.stockQuantity))
            val totalProfit = profitPerUnit.multiply(BigDecimal(salesData.totalSold))
            
            row.createCell(0).setCellValue(product.id.toString())
            row.createCell(1).setCellValue(product.name)
            row.createCell(2).setCellValue(product.barcode ?: "")
            row.createCell(3).setCellValue(product.category?.name ?: "")
            row.createCell(4).setCellValue(product.brand?.name ?: "")
            row.createCell(5).setCellValue(product.costPrice.toDouble())
            row.createCell(6).setCellValue(product.price.toDouble())
            row.createCell(7).setCellValue(product.discountPercent.toDouble())
            row.createCell(8).setCellValue(finalPrice.toDouble())
            row.createCell(9).setCellValue(profitPerUnit.toDouble())
            row.createCell(10).setCellValue(product.stockQuantity.toDouble())
            row.createCell(11).setCellValue(stockValue.toDouble())
            row.createCell(12).setCellValue(salesData.totalSold.toDouble())
            row.createCell(13).setCellValue(salesData.totalRevenue.toDouble())
            row.createCell(14).setCellValue(totalProfit.toDouble())
            row.createCell(15).setCellValue(product.imageUrl ?: "")
            row.createCell(16).setCellValue(product.createdAt.toString())
            row.createCell(17).setCellValue(product.updatedAt.toString())
        }
        
        // Auto-size columns
        for (i in 0 until headers.size) {
            sheet.autoSizeColumn(i)
        }
    }

    private fun createSummarySheet(workbook: Workbook, products: List<Product>) {
        val sheet = workbook.createSheet("Summary")
        
        var rowIndex = 0
        
        // Total products
        sheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Total Products:")
            createCell(1).setCellValue(products.size.toDouble())
        }
        
        // Total stock value
        val totalStockValue = products.sumOf { 
            it.costPrice.multiply(BigDecimal(it.stockQuantity))
        }
        sheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Total Stock Value:")
            createCell(1).setCellValue(totalStockValue.toDouble())
        }
        
        // Total potential profit
        val totalPotentialProfit = products.sumOf { 
            it.getProfit().multiply(BigDecimal(it.stockQuantity))
        }
        sheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Total Potential Profit:")
            createCell(1).setCellValue(totalPotentialProfit.toDouble())
        }
        
        // Low stock count
        val lowStockCount = products.count { it.stockQuantity <= 10 }
        sheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Low Stock Products (<= 10):")
            createCell(1).setCellValue(lowStockCount.toDouble())
        }
        
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createLowStockSheet(workbook: Workbook, products: List<Product>) {
        val sheet = workbook.createSheet("Low Stock Alert")
        val lowStockProducts = products.filter { it.stockQuantity <= 10 }
        
        // Header style
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.RED.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            })
        }
        
        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = listOf("Product Name", "Category", "Brand", "Stock Quantity", "Reorder Needed")
        
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        // Data rows
        lowStockProducts.sortedBy { it.stockQuantity }.forEachIndexed { rowIndex, product ->
            val row = sheet.createRow(rowIndex + 1)
            
            row.createCell(0).setCellValue(product.name)
            row.createCell(1).setCellValue(product.category?.name ?: "")
            row.createCell(2).setCellValue(product.brand?.name ?: "")
            row.createCell(3).setCellValue(product.stockQuantity.toDouble())
            row.createCell(4).setCellValue(if (product.stockQuantity == 0) "URGENT" else "Soon")
        }
        
        // Auto-size columns
        for (i in 0 until headers.size) {
            sheet.autoSizeColumn(i)
        }
    }

    private data class SalesData(
        val totalSold: Int,
        val totalRevenue: BigDecimal
    )
}
