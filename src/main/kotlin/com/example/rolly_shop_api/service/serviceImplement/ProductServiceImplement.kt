package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.ProductRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.Product
import com.example.rolly_shop_api.repository.BrandRepository
import com.example.rolly_shop_api.repository.CategoryRepository
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.ReviewRepository
import com.example.rolly_shop_api.service.ProductService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ProductServiceImplement(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository
) : ProductService {

    // ==================== PUBLIC METHODS (Customer View) ====================

    override fun getById(id: UUID): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Product not found") }
        val avgRating = reviewRepository.getAverageRating(id)
        return ProductResponse.from(product, avgRating)
    }

    override fun getAll(pageable: Pageable): PageResponse<ProductSimpleResponse> {
        val page = productRepository.findAll(pageable)
        return PageResponse.from(page) { ProductSimpleResponse.from(it) }
    }

    override fun getByBrand(brandId: UUID, pageable: Pageable): PageResponse<ProductSimpleResponse> {
        val page = productRepository.findByBrandId(brandId, pageable)
        return PageResponse.from(page) { ProductSimpleResponse.from(it) }
    }

    override fun getByCategory(categoryId: UUID, pageable: Pageable): PageResponse<ProductSimpleResponse> {
        val page = productRepository.findByCategoryId(categoryId, pageable)
        return PageResponse.from(page) { ProductSimpleResponse.from(it) }
    }

    override fun search(query: String, pageable: Pageable): PageResponse<ProductSimpleResponse> {
        val page = productRepository.findByNameContainingIgnoreCase(query, pageable)
        return PageResponse.from(page) { ProductSimpleResponse.from(it) }
    }

    // ==================== ADMIN METHODS (includes cost price & profit) ====================

    override fun create(request: ProductRequest): ProductAdminResponse {
        // Validate barcode uniqueness
        request.barcode?.let {
            if (productRepository.existsByBarcode(it)) {
                throw IllegalArgumentException("Product with barcode '$it' already exists")
            }
        }

        val brand = request.brandId?.let {
            brandRepository.findById(it).orElseThrow { NoSuchElementException("Brand not found") }
        }
        val category = request.categoryId?.let {
            categoryRepository.findById(it).orElseThrow { NoSuchElementException("Category not found") }
        }
        val product = Product(
            name = request.name,
            description = request.description,
            barcode = request.barcode,
            costPrice = request.costPrice,
            price = request.price,
            discountPercent = request.discountPercent,
            stockQuantity = request.stockQuantity,
            imageUrl = request.imageUrl,
            brand = brand,
            category = category
        )
        return ProductAdminResponse.from(productRepository.save(product))
    }

    override fun update(id: UUID, request: ProductRequest): ProductAdminResponse {
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Product not found") }

        // Validate barcode uniqueness (allow same barcode if it belongs to this product)
        request.barcode?.let { newBarcode ->
            val existingProduct = productRepository.findByBarcode(newBarcode)
            if (existingProduct.isPresent && existingProduct.get().id != id) {
                throw IllegalArgumentException("Product with barcode '$newBarcode' already exists")
            }
        }

        val brand = request.brandId?.let {
            brandRepository.findById(it).orElseThrow { NoSuchElementException("Brand not found") }
        }
        val category = request.categoryId?.let {
            categoryRepository.findById(it).orElseThrow { NoSuchElementException("Category not found") }
        }
        val updated = product.copy(
            name = request.name,
            description = request.description,
            barcode = request.barcode,
            costPrice = request.costPrice,
            price = request.price,
            discountPercent = request.discountPercent,
            stockQuantity = request.stockQuantity,
            imageUrl = request.imageUrl,
            brand = brand,
            category = category,
            updatedAt = Instant.now()
        )
        val avgRating = reviewRepository.getAverageRating(id)
        return ProductAdminResponse.from(productRepository.save(updated), avgRating)
    }

    override fun delete(id: UUID) {
        if (!productRepository.existsById(id)) {
            throw NoSuchElementException("Product not found")
        }
        productRepository.deleteById(id)
    }

    override fun getByIdAdmin(id: UUID): ProductAdminResponse {
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Product not found") }
        val avgRating = reviewRepository.getAverageRating(id)
        return ProductAdminResponse.from(product, avgRating)
    }

    override fun getAllAdmin(pageable: Pageable): PageResponse<ProductAdminSimpleResponse> {
        val page = productRepository.findAll(pageable)
        return PageResponse.from(page) { ProductAdminSimpleResponse.from(it) }
    }

    // ==================== BARCODE LOOKUP (for POS/scanning) ====================

    override fun getByBarcode(barcode: String): ProductAdminResponse {
        val product = productRepository.findByBarcode(barcode)
            .orElseThrow { NoSuchElementException("Product not found with barcode: $barcode") }
        val avgRating = reviewRepository.getAverageRating(product.id!!)
        return ProductAdminResponse.from(product, avgRating)
    }

    // ==================== INVENTORY STATS (Dashboard) ====================

    override fun getInventoryStats(lowStockThreshold: Int): InventoryStatsResponse {
        return InventoryStatsResponse(
            totalProducts = productRepository.countAllProducts(),
            totalValue = productRepository.getTotalInventoryValue(),
            totalPotentialProfit = productRepository.getTotalPotentialProfit(),
            lowStockCount = productRepository.countLowStock(lowStockThreshold),
            lowStockThreshold = lowStockThreshold
        )
    }

    override fun getLowStockProducts(threshold: Int, pageable: Pageable): PageResponse<ProductAdminSimpleResponse> {
        val page = productRepository.findByStockQuantityLessThanEqual(threshold, pageable)
        return PageResponse.from(page) { ProductAdminSimpleResponse.from(it) }
    }
}
