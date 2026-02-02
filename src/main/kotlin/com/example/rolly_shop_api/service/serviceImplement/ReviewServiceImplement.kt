package com.example.rolly_shop_api.service.serviceImplement

import com.example.rolly_shop_api.model.dto.request.ReviewRequest
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.dto.response.ProductReviewSummary
import com.example.rolly_shop_api.model.dto.response.ReviewResponse
import com.example.rolly_shop_api.model.entity.Review
import com.example.rolly_shop_api.repository.ProductRepository
import com.example.rolly_shop_api.repository.ReviewRepository
import com.example.rolly_shop_api.repository.UserRepository
import com.example.rolly_shop_api.service.ReviewService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ReviewServiceImplement(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) : ReviewService {

    override fun create(userId: UUID, request: ReviewRequest): ReviewResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        val product = productRepository.findById(request.productId)
            .orElseThrow { NoSuchElementException("Product not found") }

        if (reviewRepository.existsByUserUserIdAndProductId(userId, request.productId)) {
            throw IllegalArgumentException("You have already reviewed this product")
        }

        val review = Review(
            user = user,
            product = product,
            rating = request.rating,
            comment = request.comment
        )
        return ReviewResponse.from(reviewRepository.save(review))
    }

    override fun update(userId: UUID, reviewId: UUID, request: ReviewRequest): ReviewResponse {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { NoSuchElementException("Review not found") }

        if (review.user.userId != userId) {
            throw IllegalArgumentException("Review does not belong to user")
        }

        val updated = review.copy(
            rating = request.rating,
            comment = request.comment,
            updatedAt = Instant.now()
        )
        return ReviewResponse.from(reviewRepository.save(updated))
    }

    override fun delete(userId: UUID, reviewId: UUID) {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { NoSuchElementException("Review not found") }

        if (review.user.userId != userId) {
            throw IllegalArgumentException("Review does not belong to user")
        }
        reviewRepository.delete(review)
    }

    override fun getProductReviews(productId: UUID, pageable: Pageable): PageResponse<ReviewResponse> {
        val page = reviewRepository.findByProductId(productId, pageable)
        return PageResponse.from(page) { ReviewResponse.from(it) }
    }

    override fun getProductReviewSummary(productId: UUID, pageable: Pageable): ProductReviewSummary {
        val avgRating = reviewRepository.getAverageRating(productId) ?: 0.0
        val totalReviews = reviewRepository.countByProductId(productId)
        val reviews = reviewRepository.findByProductId(productId, pageable)
            .content.map { ReviewResponse.from(it) }

        return ProductReviewSummary(
            averageRating = avgRating,
            totalReviews = totalReviews,
            reviews = reviews
        )
    }

    override fun getUserReviews(userId: UUID, pageable: Pageable): PageResponse<ReviewResponse> {
        val page = reviewRepository.findByUserUserId(userId, pageable)
        return PageResponse.from(page) { ReviewResponse.from(it) }
    }
}

