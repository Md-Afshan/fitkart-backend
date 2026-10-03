package com.fitkart.service;

import com.fitkart.dto.product.ProductImageRequest;
import com.fitkart.dto.product.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse addImage(ProductImageRequest request);

    List<ProductImageResponse> getImagesByProductId(Long productId);

    ProductImageResponse getImageById(Long id);

    void deleteImage(Long id);
}