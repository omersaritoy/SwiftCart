package com.cavcav.swiftcart.review.repository;

import com.cavcav.swiftcart.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,String> {
    Page<Review> findByProductId(String productId, Pageable pageable);
    Page<Review> findByUserId(String userId, Pageable pageable);
    boolean existsByUserIdAndProductId(String userId, String productId);
    Optional<Review> findByIdAndUserId(String id, String userId);
}
