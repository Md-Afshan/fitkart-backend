package com.fitkart.controller;

import com.fitkart.dto.product.ProductImageResponse;
import com.fitkart.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> addImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId,
            @RequestParam(
                    value = "isPrimary",
                    defaultValue = "false"
            ) Boolean isPrimary
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productImageService.addImage(
                                file,
                                productId,
                                isPrimary
                        )
                );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getImagesByProductId(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productImageService.getImagesByProductId(productId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageResponse> getImageById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                productImageService.getImageById(id)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id
    ) {

        productImageService.deleteImage(id);

        return ResponseEntity.noContent().build();
    }
}