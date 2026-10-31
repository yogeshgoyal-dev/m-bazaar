package com.ecom.E_Commerce.service;

import com.ecom.E_Commerce.dto.OrderRequest;
import com.ecom.E_Commerce.dto.OrderResponse;
import com.ecom.E_Commerce.exception.ResourceNotFoundException;
import com.ecom.E_Commerce.model.*;
import com.ecom.E_Commerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new OrderResponse.OrderItemResponse(
                    item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(), item.getPrice(), subtotal
            );
        }).collect(Collectors.toList());
        return new OrderResponse(
                order.getId(), order.getUser().getId(), order.getUser().getEmail(),
                order.getTotalAmount(), order.getStatus().name(), order.getPaymentMethod(),
                order.getShippingAddress(), order.getOrderDate(), itemResponses
        );
    }

    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        // Only COD for now
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD";
        if (!paymentMethod.equalsIgnoreCase("COD")) {
            throw new RuntimeException("Only COD is supported currently");
        }
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod("COD");
        order.setStatus(OrderStatus.PLACED);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(saved);
    }

    public List<OrderResponse> getMyOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, String email, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You can only view your own orders");
        }
        return toResponse(order);
    }

    public OrderResponse cancelOrder(Long orderId, String email, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!isAdmin && !order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Only PLACED orders can be cancelled");
        }
        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse updateStatus(Long orderId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            order.setStatus(newStatus);
            return toResponse(orderRepository.save(order));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status! Use PLACED, SHIPPED, DELIVERED, CANCELLED");
        }
    }
}
