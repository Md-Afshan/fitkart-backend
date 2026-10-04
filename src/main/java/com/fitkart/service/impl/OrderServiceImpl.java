package com.fitkart.service.impl;

import com.fitkart.dto.order.OrderItemResponse;
import com.fitkart.dto.order.OrderResponse;
import com.fitkart.dto.order.PlaceOrderResponse;
import com.fitkart.dto.order.UpdateOrderStatusRequest;
import com.fitkart.entity.Cart;
import com.fitkart.entity.CartItem;
import com.fitkart.entity.Order;
import com.fitkart.entity.OrderItem;
import com.fitkart.entity.OrderStatus;
import com.fitkart.entity.Product;
import com.fitkart.entity.ProductStatus;
import com.fitkart.entity.User;
import com.fitkart.exception.CartEmptyException;
import com.fitkart.exception.InvalidOrderStatusException;
import com.fitkart.exception.ProductUnavailableException;
import com.fitkart.exception.ResourceNotFoundException;
import com.fitkart.exception.StockUnavailableException;
import com.fitkart.exception.UnauthorizedAccessException;
import com.fitkart.repository.CartRepository;
import com.fitkart.repository.OrderItemRepository;
import com.fitkart.repository.OrderRepository;
import com.fitkart.repository.ProductRepository;
import com.fitkart.repository.UserRepository;
import com.fitkart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public PlaceOrderResponse placeOrder() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for current user"
                        )
                );

        if (cart.getCartItems().isEmpty()) {
            throw new CartEmptyException(
                    "Cannot place order with an empty cart"
            );
        }

        /*
         * First validate all products and stock.
         * This prevents us from changing anything before
         * knowing that the complete order can be placed.
         */
        for (CartItem cartItem : cart.getCartItems()) {

            Product product = cartItem.getProduct();

            validateProduct(product);
            validateStock(product, cartItem.getQuantity());
        }

        /*
         * Create Order
         */
        Order order = new Order();

        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderDate(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        /*
         * Create OrderItems and calculate total
         */
        for (CartItem cartItem : cart.getCartItems()) {

            Product product = cartItem.getProduct();

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            /*
             * Store the price at the time of purchase.
             * Future product price changes will not affect this order.
             */
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            orderItem.setCreatedAt(LocalDateTime.now());

            order.getOrderItems().add(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        /*
         * Save Order first so the Order ID exists.
         */
        Order savedOrder = orderRepository.save(order);

        /*
         * Save all OrderItems and reduce product stock.
         */
        for (OrderItem orderItem : order.getOrderItems()) {

            orderItemRepository.save(orderItem);

            Product product = orderItem.getProduct();

            int remainingStock =
                    product.getStockQuantity()
                            - orderItem.getQuantity();

            product.setStockQuantity(remainingStock);

            /*
             * Automatically mark product as OUT_OF_STOCK
             * when stock reaches zero.
             */
            if (remainingStock == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }

            productRepository.save(product);
        }

        /*
         * Clear the customer's cart after successful order creation.
         */
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToPlaceOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        User user = getCurrentUser();

        List<Order> orders =
                orderRepository.findByUserOrderByOrderDateDesc(user);

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        /*
         * Customer can only view their own order.
         */
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(
                    "Order does not belong to the current user"
            );
        }

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        return mapToOrderResponse(savedOrder);
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        /*
         * Prevent updating to the same status.
         *
         * This check comes first so that:
         *
         * DELIVERED → DELIVERED
         * returns "Order is already in DELIVERED status"
         *
         * instead of the more general final-status message.
         */
        if (currentStatus == newStatus) {

            throw new InvalidOrderStatusException(
                    "Order is already in "
                            + currentStatus
                            + " status"
            );
        }

        /*
         * Delivered orders are final.
         */
        if (currentStatus == OrderStatus.DELIVERED) {

            throw new InvalidOrderStatusException(
                    "Delivered order cannot change status"
            );
        }

        /*
         * Cancelled orders are final.
         */
        if (currentStatus == OrderStatus.CANCELLED) {

            throw new InvalidOrderStatusException(
                    "Cancelled order cannot change status"
            );
        }

        /*
         * PLACED → CONFIRMED
         * PLACED → CANCELLED
         */
        if (currentStatus == OrderStatus.PLACED) {

            if (newStatus == OrderStatus.CONFIRMED ||
                    newStatus == OrderStatus.CANCELLED) {
                return;
            }
        }

        /*
         * CONFIRMED → SHIPPED
         * CONFIRMED → CANCELLED
         */
        if (currentStatus == OrderStatus.CONFIRMED) {

            if (newStatus == OrderStatus.SHIPPED ||
                    newStatus == OrderStatus.CANCELLED) {
                return;
            }
        }

        /*
         * SHIPPED → DELIVERED
         */
        if (currentStatus == OrderStatus.SHIPPED) {

            if (newStatus == OrderStatus.DELIVERED) {
                return;
            }
        }

        /*
         * Any other transition is invalid.
         */
        throw new InvalidOrderStatusException(
                "Invalid order status transition from "
                        + currentStatus
                        + " to "
                        + newStatus
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new UnauthorizedAccessException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: "
                                        + email
                        )
                );
    }

    private void validateProduct(Product product) {

        if (product.getStatus() != ProductStatus.ACTIVE) {

            throw new ProductUnavailableException(
                    "Product is not available for purchase"
            );
        }
    }

    private void validateStock(
            Product product,
            int quantity
    ) {

        if (quantity > product.getStockQuantity()) {

            throw new StockUnavailableException(
                    "Requested quantity exceeds available stock"
            );
        }
    }

    private String generateOrderNumber() {

        return "FK-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20);
    }

    private PlaceOrderResponse mapToPlaceOrderResponse(
            Order order
    ) {

        PlaceOrderResponse response =
                new PlaceOrderResponse();

        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());

        return response;
    }

    private OrderResponse mapToOrderResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(this::mapToOrderItemResponse)
                        .toList();

        OrderResponse response =
                new OrderResponse();

        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setOrderDate(order.getOrderDate());
        response.setItems(items);

        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(
            OrderItem orderItem
    ) {

        Product product = orderItem.getProduct();

        OrderItemResponse response =
                new OrderItemResponse();

        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setQuantity(orderItem.getQuantity());

        /*
         * IMPORTANT:
         * Use stored order price, NOT current product price.
         */
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setSubtotal(orderItem.getSubtotal());

        return response;
    }
}