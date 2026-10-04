package com.fitkart.service.impl;

import com.fitkart.exception.ResourceNotFoundException;
import com.fitkart.dto.product.ProductRequest;
import com.fitkart.dto.product.ProductResponse;
import com.fitkart.entity.Category;
import com.fitkart.entity.Product;
import com.fitkart.entity.ProductStatus;
import com.fitkart.repository.CategoryRepository;
import com.fitkart.repository.ProductRepository;
import com.fitkart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found with id: " + request.getCategoryId()
                        )
                );

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(request.getBrand());
        product.setStatus(request.getStatus());
        product.setCategory(category);

        LocalDateTime now = LocalDateTime.now();

        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> getAllProducts(
            String search,
            Long categoryId
    ) {

        List<ProductStatus> visibleStatuses = List.of(
                ProductStatus.ACTIVE,
                ProductStatus.OUT_OF_STOCK
        );

        List<Product> products;

        if (search != null && !search.isBlank() && categoryId != null) {

            products = productRepository
                    .findByStatusInAndCategory_IdAndNameContainingIgnoreCase(
                            visibleStatuses,
                            categoryId,
                            search
                    );

        } else if (search != null && !search.isBlank()) {

            products = productRepository
                    .findByStatusInAndNameContainingIgnoreCase(
                            visibleStatuses,
                            search
                    );

        } else if (categoryId != null) {

            products = productRepository
                    .findByStatusInAndCategory_Id(
                            visibleStatuses,
                            categoryId
                    );

        } else {

            products = productRepository.findByStatusIn(visibleStatuses);
        }

        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        String role = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        if ("ROLE_CUSTOMER".equals(role)) {

            if (product.getStatus() == ProductStatus.INACTIVE
                    || product.getStatus() == ProductStatus.DISCONTINUED) {

                throw new ResourceNotFoundException(
                        "Product not found with id: " + id
                );
            }
        }

        return mapToResponse(product);
    }

    @Override
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + id
                        )
                );

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found with id: " + request.getCategoryId()
                        )
                );

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(request.getBrand());
        product.setStatus(request.getStatus());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: " + id
                        )
                );

        product.setStatus(ProductStatus.INACTIVE);

        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setBrand(product.getBrand());
        response.setStockQuantity(product.getStockQuantity());
        response.setStatus(product.getStatus());

        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());

        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
}