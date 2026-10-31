package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.OrderResponse;
import com.ecom.E_Commerce.dto.ProductResponse;
import com.ecom.E_Commerce.model.Order;
import com.ecom.E_Commerce.model.Product;
import com.ecom.E_Commerce.repository.OrderRepository;
import com.ecom.E_Commerce.repository.ProductRepository;
import com.ecom.E_Commerce.security.UserPrincipal;
import com.ecom.E_Commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasAnyRole('SELLER','ADMIN')")
public class SellerController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Product> products;
        if (isAdmin) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findBySellerId(principal.getId());
        }
        List<ProductResponse> resp = products.stream().map(p -> {
            ProductResponse r = new ProductResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setDescription(p.getDescription());
            r.setPrice(p.getPrice());
            r.setStock(p.getStock());
            r.setImageUrl(p.getImageUrl());
            r.setCategoryId(p.getCategory().getId());
            r.setCategoryName(p.getCategory().getName());
            if (p.getSeller() != null) { r.setSellerId(p.getSeller().getId()); r.setSellerName(p.getSeller().getName()); }
            r.setCreatedAt(p.getCreatedAt());
            return r;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Order> allOrders = orderRepository.findAll();
        List<Order> sellerOrders = new ArrayList<>();
        for (Order order : allOrders) {
            boolean containsSellerProduct = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getSeller() != null &&
                            (isAdmin || item.getProduct().getSeller().getId().equals(principal.getId())));
            if (containsSellerProduct) {
                sellerOrders.add(order);
            }
        }
        List<OrderResponse> resp = sellerOrders.stream().map(order -> {
            List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                    .filter(item -> isAdmin || (item.getProduct().getSeller() != null && item.getProduct().getSeller().getId().equals(principal.getId())))
                    .map(item -> new OrderResponse.OrderItemResponse(
                            item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(), item.getPrice(),
                            item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    )).collect(Collectors.toList());
            BigDecimal total = items.stream().map(OrderResponse.OrderItemResponse::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getEmail(), total, order.getStatus().name(), order.getPaymentMethod(), order.getShippingAddress(), order.getOrderDate(), items);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Product> myProducts = isAdmin ? productRepository.findAll() : productRepository.findBySellerId(principal.getId());
        int totalProducts = myProducts.size();
        int totalStock = myProducts.stream().mapToInt(Product::getStock).sum();

        // Calculate sold
        List<Order> allOrders = orderRepository.findAll();
        int totalSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Set<Long> orderIds = new HashSet<>();
        for (Order order : allOrders) {
            for (var item : order.getItems()) {
                Product p = item.getProduct();
                if (p.getSeller() != null && (isAdmin || p.getSeller().getId().equals(principal.getId()))) {
                    if (order.getStatus() != com.ecom.E_Commerce.model.OrderStatus.CANCELLED) {
                        totalSold += item.getQuantity();
                        totalRevenue = totalRevenue.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        orderIds.add(order.getId());
                    }
                }
            }
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("totalStock", totalStock);
        stats.put("totalSold", totalSold);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", orderIds.size());
        return ResponseEntity.ok(stats);
    }
}
