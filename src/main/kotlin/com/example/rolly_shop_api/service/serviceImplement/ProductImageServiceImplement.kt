package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.ImageReorderRequest
import com.example.rolly_shop_api.model.dto.request.ProductImageRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.model.entity.ProductImage
import com.example.rolly_shop_api.repository.ProductImageRepository
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.service.ProductImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ProductImageServiceImplement(
    private val productImageRepository: ProductImageRepository,
    private val productRepository: ProductRepository
) : ProductImageService {

    override fun getAllImages(productId: UUID): List<ProductImageResponse> {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val images = productImageRepository.findByProductIdOrderBySortOrder(productId)
        return images.map { ProductImageResponse.from(it) }
    }

    @Transactional
    override fun addImage(productId: UUID, request: ProductImageRequest): ProductImageResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        // Get current max sort order if displayOrder not provided
        val sortOrder = request.displayOrder ?: run {
            val existingImages = productImageRepository.findByProductIdOrderBySortOrder(productId)
            if (existingImages.isEmpty()) 0 else existingImages.maxOf { it.sortOrder } + 1
        }
        
        // If setting as primary, unset all other primary images
        if (request.isPrimary) {
            val existingImages = productImageRepository.findByProductIdOrderBySortOrder(productId)
            existingImages.forEach { img ->
                if (img.isPrimary) {
                    img.isPrimary = false
                    img.updatedAt = Instant.now()
                    productImageRepository.save(img)
                }
            }
        }
        
        val newImage = ProductImage(
            product = product,
            imageUrl = request.url,
            isPrimary = request.isPrimary,
            sortOrder = sortOrder
        )
        
        val savedImage = productImageRepository.save(newImage)
        return ProductImageResponse.from(savedImage)
    }

    @Transactional
    override fun setPrimaryImage(productId: UUID, imageId: UUID): SetPrimaryImageResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val newPrimaryImage = productImageRepository.findById(imageId)
            .orElseThrow { NoSuchElementException("Image not found with id: $imageId") }
        
        if (newPrimaryImage.product.id != productId) {
            throw IllegalArgumentException("Image does not belong to this product")
        }
        
        // Find current primary image
        val previousPrimaryImage = productImageRepository.findByProductIdAndIsPrimaryTrue(productId)
        
        // Unset all primary images
        val allImages = productImageRepository.findByProductIdOrderBySortOrder(productId)
        allImages.forEach { img ->
            if (img.isPrimary) {
                img.isPrimary = false
                img.updatedAt = Instant.now()
                productImageRepository.save(img)
            }
        }
        
        // Set new primary image
        newPrimaryImage.isPrimary = true
        newPrimaryImage.updatedAt = Instant.now()
        productImageRepository.save(newPrimaryImage)
        
        return SetPrimaryImageResponse(
            productId = productId.toString(),
            previousPrimaryImageId = previousPrimaryImage?.id?.toString(),
            newPrimaryImageId = imageId.toString()
        )
    }

    @Transactional
    override fun reorderImages(productId: UUID, request: ImageReorderRequest): ImageReorderResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        var updatedCount = 0
        
        request.imageOrders.forEach { orderItem ->
            val imageId = UUID.fromString(orderItem.imageId)
            val image = productImageRepository.findById(imageId)
                .orElseThrow { NoSuchElementException("Image not found with id: $imageId") }
            
            if (image.product.id != productId) {
                throw IllegalArgumentException("Image ${orderItem.imageId} does not belong to product $productId")
            }
            
            image.sortOrder = orderItem.displayOrder
            image.updatedAt = Instant.now()
            productImageRepository.save(image)
            updatedCount++
        }
        
        return ImageReorderResponse(
            productId = productId.toString(),
            updatedCount = updatedCount
        )
    }

    @Transactional
    override fun deleteImage(productId: UUID, imageId: UUID): DeleteImageResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val image = productImageRepository.findById(imageId)
            .orElseThrow { NoSuchElementException("Image not found with id: $imageId") }
        
        if (image.product.id != productId) {
            throw IllegalArgumentException("Image does not belong to this product")
        }
        
        // Check if this is the only image
        val allImages = productImageRepository.findByProductIdOrderBySortOrder(productId)
        if (allImages.size == 1) {
            throw IllegalStateException("Cannot delete the only image. Products must have at least one image.")
        }
        
        val wasPrimary = image.isPrimary
        
        // Delete the image
        productImageRepository.delete(image)
        
        // If deleted image was primary, set first remaining image as primary
        if (wasPrimary) {
            val remainingImages = productImageRepository.findByProductIdOrderBySortOrder(productId)
            if (remainingImages.isNotEmpty()) {
                val firstImage = remainingImages.first()
                firstImage.isPrimary = true
                firstImage.updatedAt = Instant.now()
                productImageRepository.save(firstImage)
            }
        }
        
        return DeleteImageResponse(deletedImageId = imageId.toString())
    }
}
