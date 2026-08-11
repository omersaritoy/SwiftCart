package com.cavcav.swiftcart.product.repository;

import com.cavcav.swiftcart.product.model.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(String categoryId, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(String id);

    Page<Product> findBySellerId(String id, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
            "WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") String productId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p Set p.stock=p.stock+:quantity where p.id=:productId")
    void increaseStock(@Param("productId") String productId, @Param("quantity") int quantity);}
