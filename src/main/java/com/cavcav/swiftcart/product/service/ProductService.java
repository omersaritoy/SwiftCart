package com.cavcav.swiftcart.product.service;


import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateStockRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.dto.response.ProductSummaryResponse;
import com.cavcav.swiftcart.user.model.User;


//POST   /api/v1/products                    (seller)
//PUT    /api/v1/products/{id}               (seller)
//DELETE /api/v1/products/{id}               (seller)
//PATCH  /api/v1/products/{id}/stock         (seller)
//PATCH  /api/v1/products/{id}/status        (seller)

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request, User seller);

    PaginationResponse<ProductSummaryResponse> getProducts(int page, int size, String sortBy, String direction, String categoryId);

    ProductResponse getProductById(String id);
    PaginationResponse<ProductSummaryResponse> getMyProducts(int page,int size,User seller);
    ProductResponse updateProduct(UpdateProductRequest request, String productId, User seller);
    String deleteProductById(String id, User seller);
    ProductResponse updateStockById(UpdateStockRequest request, String productId, User seller);
    ProductResponse updateStatus(String productId, User seller);
}
