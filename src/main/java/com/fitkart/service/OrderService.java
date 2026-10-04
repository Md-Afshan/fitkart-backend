package com.fitkart.service;

import com.fitkart.dto.order.OrderResponse;
import com.fitkart.dto.order.PlaceOrderResponse;
import com.fitkart.dto.order.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {

    PlaceOrderResponse placeOrder();

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getAllOrders(String search);

    OrderResponse updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    );
}