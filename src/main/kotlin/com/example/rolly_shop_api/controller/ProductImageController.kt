package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.ImageReorderRequest
import com.example.rolly_shop_api.model.dto.request.ProductImageRequest
import com.example.rolly_shop_api.model.dto.response.*
import com.example.rolly_shop_api.service.ProductImageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Images", description = "Multi-image support for products")
class ProductImageController(
    private val productImageService: ProductImageService
) {

    @GetMapping("/{productId}/images")
    @Operation(summary = "Get all images for a product")
    fun getAllImages(@PathVariable productId: UUID): BaseResponse<List<ProductImageResponse>> {
        val images = productImageService.getAllImages(productId)
        return BaseResponse.success(
            data = images,
            message = "Product images retrieved successfully"
        )
    }

    @PostMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new image to a product")
    fun addImage(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: ProductImageRequest
    ): BaseResponse<ProductImageResponse> {
        val image = productImageService.addImage(productId, request)
        return BaseResponse.success(
            data = image,
            message = "Image added successfully"
        )
    }

    @PutMapping("/{productId}/images/{imageId}/set-primary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set an image as primary (main product image)")
    fun setPrimaryImage(
        @PathVariable productId: UUID,
        @PathVariable imageId: UUID
    ): BaseResponse<SetPrimaryImageResponse> {
        val result = productImageService.setPrimaryImage(productId, imageId)
        return BaseResponse.success(
            data = result,
            message = "Primary image updated successfully"
        )
    }

    @PutMapping("/{productId}/images/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reorder images")
    fun reorderImages(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: ImageReorderRequest
    ): BaseResponse<ImageReorderResponse> {
        val result = productImageService.reorderImages(productId, request)
        return BaseResponse.success(
            data = result,
            message = "Image order updated successfully"
        )
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an image")
    fun deleteImage(
        @PathVariable productId: UUID,
        @PathVariable imageId: UUID
    ): BaseResponse<DeleteImageResponse> {
        val result = productImageService.deleteImage(productId, imageId)
        return BaseResponse.success(
            data = result,
            message = "Image deleted successfully"
        )
    }
}
