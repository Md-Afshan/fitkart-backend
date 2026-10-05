package com.fitkart.service.impl;

import com.fitkart.dto.product.ProductImageResponse;
import com.fitkart.entity.Product;
import com.fitkart.entity.ProductImage;
import com.fitkart.exception.ResourceNotFoundException;
import com.fitkart.repository.ProductImageRepository;
import com.fitkart.repository.ProductRepository;
import com.fitkart.service.FileStorageService;
import com.fitkart.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Override
    public ProductImageResponse addImage(
            MultipartFile file,
            Long productId,
            Boolean isPrimary
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId
                        )
                );

        /*
         * If this image is being marked as primary,
         * remove the primary status from the existing image.
         */
        if (Boolean.TRUE.equals(isPrimary)) {

            productImageRepository
                    .findByProductIdAndIsPrimaryTrue(product.getId())
                    .ifPresent(existingPrimaryImage ->
                            existingPrimaryImage.setIsPrimary(false)
                    );
        }

        // Store the actual image file.
        String imagePath = fileStorageService.storeProductImage(file);

        ProductImage productImage = new ProductImage();

        productImage.setImagePath(imagePath);
        productImage.setIsPrimary(
                Boolean.TRUE.equals(isPrimary)
        );
        productImage.setProduct(product);
        productImage.setCreatedAt(LocalDateTime.now());

        ProductImage savedImage =
                productImageRepository.save(productImage);

        return mapToResponse(savedImage);
    }

    @Override
    public List<ProductImageResponse> getImagesByProductId(
            Long productId
    ) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        return productImageRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductImageResponse getImageById(Long id) {

        ProductImage productImage =
                productImageRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product image not found with id: " + id
                                )
                        );

        return mapToResponse(productImage);
    }

    @Override
    public void deleteImage(Long id) {

        ProductImage productImage =
                productImageRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product image not found with id: " + id
                                )
                        );

        productImageRepository.delete(productImage);
    }

    private ProductImageResponse mapToResponse(
            ProductImage productImage
    ) {

        ProductImageResponse response =
                new ProductImageResponse();

        response.setId(productImage.getId());
        response.setImagePath(productImage.getImagePath());
        response.setIsPrimary(productImage.getIsPrimary());
        response.setProductId(productImage.getProduct().getId());
        response.setCreatedAt(productImage.getCreatedAt());

        return response;
    }
}