package com.cavcav.swiftcart.review.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.order.model.OrderItem;
import com.cavcav.swiftcart.order.repository.OrderItemRepository;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.review.dto.request.CreateReviewRequest;
import com.cavcav.swiftcart.review.dto.request.UpdateReviewRequest;
import com.cavcav.swiftcart.review.dto.response.ReviewResponse;
import com.cavcav.swiftcart.review.model.Review;
import com.cavcav.swiftcart.review.repository.ReviewRepository;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;


    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, User user) {
        log.info("Creating review: productId={}, userId={}", request.productId(), user.getId());

        Product product = productRepository.findByIdAndIsActiveTrue(request.productId()).orElseThrow(() -> {
            log.warn("Product not found: id={}", request.productId());
            return new BusinessException(
                    "Product not found",
                    "PRODUCT_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );

        });
        boolean hasPurchased = orderItemRepository.hasUserPurchasedProduct(user.getId(), request.productId());

        if (!hasPurchased) {
            log.warn("User has not purchased product: userId={}, productId={}", user.getId(), request.productId());
            throw new BusinessException(
                    "You can only review products you have purchased",
                    "PRODUCT_NOT_PURCHASED",
                    HttpStatus.FORBIDDEN);
        }
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), request.productId())) {
            log.warn("Review already exists: userId={}, productId={}", user.getId(), request.productId());
            throw new BusinessException(
                    "You have already reviewed this product",
                    "REVIEW_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );

        }
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created: id={}, productId={}, userId={}", saved.getId(), request.productId(), user.getId());

        return ReviewResponse.from(saved);
    }

    @Transactional
    public ReviewResponse updateReview(String reviewId, UpdateReviewRequest request, User user) {
        log.info("Updating review: reviewId={}, userId={}", reviewId, user.getId());
        Review review = reviewRepository.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Review not found: reviewId={}, userId={}", reviewId, user.getId());
                    return new BusinessException(
                            "Review not found",
                            "REVIEW_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
        if (request.rating() != null) review.setRating(request.rating());
        if (request.comment() != null) review.setComment(request.comment());
        Review updated = reviewRepository.save(review);
        log.info("Review updated: reviewId={}, userId={}", reviewId, user.getId());

        return ReviewResponse.from(updated);

    }

    @Transactional
    public void deleteReview(String reviewId, User user) {
        log.info("Deleting review: reviewId={}, userId={}", reviewId, user.getId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Review not found: reviewId={}", reviewId);
                    return new BusinessException(
                            "Review not found",
                            "REVIEW_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            log.warn("Unauthorized review deletion: reviewId={}, userId={}", reviewId, user.getId());
            throw new BusinessException(
                    "You are not authorized to delete this review",
                    "REVIEW_ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        reviewRepository.delete(review);
        log.info("Review deleted: reviewId={}, userId={}", reviewId, user.getId());
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ReviewResponse> getProductReviews(String productId, int page, int size) {
        log.info("Fetching product reviews: productId={}, page={}, size={}", productId, page, size);

        if (!productRepository.existsById(productId)) {
            log.warn("Product not found: productId={}", productId);
            throw new BusinessException(
                    "Product not found",
                    "PRODUCT_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        log.info("Product reviews fetched: productId={}, total={}", productId, reviews.getTotalElements());
        return PaginationResponse.of(reviews.map(ReviewResponse::from));

    }

    @Transactional(readOnly = true)
    public PaginationResponse<ReviewResponse> getMyReviews(int page, int size, User user) {
        log.info("Fetching user reviews: userId={}, page={}, size={}", user.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByUserId(user.getId(), pageable);

        log.info("User reviews fetched: userId={}, total={}", user.getId(), reviews.getTotalElements());
        return PaginationResponse.of(reviews.map(ReviewResponse::from));
    }
}
