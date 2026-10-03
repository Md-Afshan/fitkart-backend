package com.fitkart.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductImageRequest {

    @NotBlank(message = "Image path is required.")
    @Size(max = 500, message = "Image path must not exceed 500 characters.")
    private String imagePath;

    private Boolean isPrimary = false;

    @NotNull(message = "Product ID is required.")
    private Long productId;
}