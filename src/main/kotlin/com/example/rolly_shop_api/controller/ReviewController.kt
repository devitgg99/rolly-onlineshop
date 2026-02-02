package com.example.rolly_shop_api.controller

import com.example.rolly_shop_api.model.dto.request.ReviewRequest
import com.example.rolly_shop_api.model.dto.response.BaseResponse
import com.example.rolly_shop_api.model.dto.response.PageResponse
import com.example.rolly_shop_api.model.dto.response.ProductReviewSummary
import com.example.rolly_shop_api.model.dto.response.ReviewResponse
import com.example.rolly_shop_api.service.CurrentUserService
import com.example.rolly_shop_api.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Product reviews and ratings")
class ReviewController(
    private val reviewService: ReviewService,
    private val currentUserService: CurrentUserService
) {
    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/product/{productId}")
    @SecurityRequirements
    @Operation(
        summary = "Get product reviews",
        description = "🌐 PUBLIC - Get all reviews for a product with rating summary"
    )
    fun getProductReviews(
        @PathVariable productId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<ProductReviewSummary> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(
            reviewService.getProductReviewSummary(productId, pageable),
            "Product reviews"
        )
    }

    // ==================== USER ENDPOINTS ====================

    @PostMapping
    @Operation(
        summary = "Create review",
        description = "👤 USER - Add review for a product (one review per product per user)"
    )
    fun create(@Valid @RequestBody request: ReviewRequest): BaseResponse<ReviewResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(reviewService.create(userId, request), "Review created")
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update my review",
        description = "👤 USER - Update your own review"
    )
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ReviewRequest
    ): BaseResponse<ReviewResponse> {
        val userId = currentUserService.getCurrentUserId()
        return BaseResponse.success(reviewService.update(userId, id, request), "Review updated")
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete my review",
        description = "👤 USER - Delete your own review"
    )
    fun delete(@PathVariable id: UUID): BaseResponse<Unit> {
        val userId = currentUserService.getCurrentUserId()
        reviewService.delete(userId, id)
        return BaseResponse.ok("Review deleted")
    }

    @GetMapping("/my")
    @Operation(
        summary = "Get my reviews",
        description = "👤 USER - Get all reviews written by logged-in user"
    )
    fun getMyReviews(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): BaseResponse<PageResponse<ReviewResponse>> {
        val userId = currentUserService.getCurrentUserId()
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return BaseResponse.success(reviewService.getUserReviews(userId, pageable), "My reviews")
    }
}
