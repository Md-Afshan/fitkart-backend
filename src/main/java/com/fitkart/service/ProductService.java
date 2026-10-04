package com.fitkart.service;

import com.fitkart.dto.product.ProductRequest;
import com.fitkart.dto.product.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts(String search, Long categoryId);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}