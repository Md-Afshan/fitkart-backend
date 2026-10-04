package com.fitkart.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlaceOrderResponse {

    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String status;
}