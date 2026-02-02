package com.example.rolly_shop_api.model.dto.response

import com.example.rolly_shop_api.model.entity.Review
import java.time.Instant
import java.util.*

data class ReviewResponse(
    val id: UUID,
    val productId: UUID,
    val userId: UUID,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(review: Review) = ReviewResponse(
            id = review.id!!,
            productId = review.product.id!!,
            userId = review.user.userId!!,
            userName = review.user.fullName,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.createdAt
        )
    }
}

data class ProductReviewSummary(
    val averageRating: Double,
    val totalReviews: Long,
    val reviews: List<ReviewResponse>
)

