package com.fitkart.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductImageResponse {

    private Long id;

    private String imagePath;

    private Boolean isPrimary;

    private Long productId;

    private LocalDateTime createdAt;
}