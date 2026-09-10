package com.cavcav.swiftcart.review.controller;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.review.dto.request.CreateReviewRequest;
import com.cavcav.swiftcart.review.dto.request.UpdateReviewRequest;
import com.cavcav.swiftcart.review.dto.response.ReviewResponse;
import com.cavcav.swiftcart.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.createReview(request, principal.user())));
    }
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<PaginationResponse<ReviewResponse>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getProductReviews(productId, page, size)));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginationResponse<ReviewResponse>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMyReviews(page, size, principal.user())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable String id,
            @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.updateReview(id, request, principal.user())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.deleteReview(id, principal.user());
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

}
