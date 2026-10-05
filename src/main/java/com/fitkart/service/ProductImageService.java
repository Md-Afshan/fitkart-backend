package com.fitkart.service;

import com.fitkart.dto.product.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse addImage(
            MultipartFile file,
            Long productId,
            Boolean isPrimary
    );

    List<ProductImageResponse> getImagesByProductId(Long productId);

    ProductImageResponse getImageById(Long id);

    void deleteImage(Long id);
}