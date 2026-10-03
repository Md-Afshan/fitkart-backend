package com.fitkart.controller;

import com.fitkart.dto.product.ProductImageRequest;
import com.fitkart.dto.product.ProductImageResponse;
import com.fitkart.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> addImage(
            @Valid @RequestBody ProductImageRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productImageService.addImage(request));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<ProductImageResponse>> getImagesByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productImageService.getImagesByProductId(productId)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ProductImageResponse> getImageById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productImageService.getImageById(id)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id) {

        productImageService.deleteImage(id);

        return ResponseEntity.noContent().build();
    }
}