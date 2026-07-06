package com.cavcav.swiftcart.product.repository;

import com.cavcav.swiftcart.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(String categoryId, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(String id);

    Page<Product> findBySellerId(String id, Pageable pageable);
}
