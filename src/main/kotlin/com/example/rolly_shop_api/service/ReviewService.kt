package com.example.rolly_shop_api.service

import com.example.rolly_shop_api.model.dto.request.ReviewRequest
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.dto.response.ProductReviewSummary
import com.example.rolly_shop_api.model.dto.response.ReviewResponse
import org.springframework.data.domain.Pageable
import java.util.*

interface ReviewService {
    fun create(userId: UUID, request: ReviewRequest): ReviewResponse
    fun update(userId: UUID, reviewId: UUID, request: ReviewRequest): ReviewResponse
    fun delete(userId: UUID, reviewId: UUID)
    fun getProductReviews(productId: UUID, pageable: Pageable): PageResponse<ReviewResponse>
    fun getProductReviewSummary(productId: UUID, pageable: Pageable): ProductReviewSummary
    fun getUserReviews(userId: UUID, pageable: Pageable): PageResponse<ReviewResponse>
}

