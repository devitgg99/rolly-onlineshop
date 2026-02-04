package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.ImageReorderRequest
import com.example.rolly_shop_api.model.dto.request.ProductImageRequest
import com.example.rolly_shop_api.model.dto.response.*
import java.util.*

interface ProductImageService {
    fun getAllImages(productId: UUID): List<ProductImageResponse>
    fun addImage(productId: UUID, request: ProductImageRequest): ProductImageResponse
    fun setPrimaryImage(productId: UUID, imageId: UUID): SetPrimaryImageResponse
    fun reorderImages(productId: UUID, request: ImageReorderRequest): ImageReorderResponse
    fun deleteImage(productId: UUID, imageId: UUID): DeleteImageResponse
}
