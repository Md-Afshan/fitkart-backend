package com.fitkart.repository;

import com.fitkart.entity.Product;
import com.fitkart.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatusIn(
            List<ProductStatus> statuses
    );

    List<Product> findByStatusInAndNameContainingIgnoreCase(
            List<ProductStatus> statuses,
            String name
    );

    List<Product> findByStatusInAndCategory_Id(
            List<ProductStatus> statuses,
            Long categoryId
    );

    List<Product> findByStatusInAndCategory_IdAndNameContainingIgnoreCase(
            List<ProductStatus> statuses,
            Long categoryId,
            String name
    );

    boolean existsByCategory_Id(Long categoryId);
}