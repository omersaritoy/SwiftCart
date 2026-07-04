package com.cavcav.swiftcart.product.service;


import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.user.model.User;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request, User seller);
}
