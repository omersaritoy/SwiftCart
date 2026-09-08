package com.cavcav.swiftcart.review.dto.response;

import com.cavcav.swiftcart.review.model.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        String id,
        String userId,
        String productId,
        String productName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}