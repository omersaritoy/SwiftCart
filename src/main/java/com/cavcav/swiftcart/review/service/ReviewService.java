package com.cavcav.swiftcart.review.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.order.model.OrderItem;
import com.cavcav.swiftcart.order.repository.OrderItemRepository;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.review.dto.request.CreateReviewRequest;
import com.cavcav.swiftcart.review.dto.response.ReviewResponse;
import com.cavcav.swiftcart.review.model.Review;
import com.cavcav.swiftcart.review.repository.ReviewRepository;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("Creating review: productId={}, userId={}",request.productId(),user.getId());

        Product product=productRepository.findByIdAndIsActiveTrue(request.productId()).orElseThrow(()->{
            log.warn("Product not found: id={}",request.productId());
            return new BusinessException(
                    "Product not found",
                    "PRODUCT_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );

        });
        boolean hasPurchased= orderItemRepository.hasUserPurchasedProduct(user.getId(),request.productId());

        if(!hasPurchased){
            log.warn("User has not purchased product: userId={}, productId={}",user.getId(),request.productId());
            throw new BusinessException(
                    "You can only review products you have purchased",
                    "PRODUCT_NOT_PURCHASED",
                    HttpStatus.FORBIDDEN);
        }
        if(reviewRepository.existsByUserIdAndProductId(user.getId(), request.productId())){
            log.warn("Review already exists: userId={}, productId={}", user.getId(), request.productId());
            throw new BusinessException(
                    "You have already reviewed this product",
                    "REVIEW_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );

        }
        Review review=Review.builder()
                .user(user)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved=reviewRepository.save(review);
        log.info("Review created: id={}, productId={}, userId={}", saved.getId(), request.productId(), user.getId());

        return ReviewResponse.from(saved);
    }
}
