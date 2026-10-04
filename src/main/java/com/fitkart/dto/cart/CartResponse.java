package com.fitkart.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal total;
}