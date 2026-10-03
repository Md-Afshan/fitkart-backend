package com.fitkart.dto.product;

import com.fitkart.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required.")
    @Size(max = 150, message = "Product name must not exceed 150 characters.")
    private String name;

    @NotBlank(message = "Product description is required.")
    private String description;

    @NotNull(message = "Product price is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Product price cannot be negative.")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required.")
    @Min(value = 0, message = "Stock quantity cannot be negative.")
    private Integer stockQuantity;

    @NotBlank(message = "Product brand is required.")
    @Size(max = 100, message = "Product brand must not exceed 100 characters.")
    private String brand;

    @NotNull(message = "Product status is required.")
    private ProductStatus status;

    @NotNull(message = "Category is required.")
    private Long categoryId;
}