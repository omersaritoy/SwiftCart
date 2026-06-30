package com.cavcav.swiftcart.product.repository;

import com.cavcav.swiftcart.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
