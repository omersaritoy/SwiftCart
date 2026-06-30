package com.cavcav.swiftcart.product.repository;

import com.cavcav.swiftcart.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
