package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.ApiResponse;
import com.ecom.E_Commerce.dto.RegisterRequest;
import com.ecom.E_Commerce.model.Order;
import com.ecom.E_Commerce.model.OrderStatus;
import com.ecom.E_Commerce.model.Product;
import com.ecom.E_Commerce.model.User;
import com.ecom.E_Commerce.repository.OrderRepository;
import com.ecom.E_Commerce.repository.ProductRepository;
import com.ecom.E_Commerce.repository.UserRepository;
import com.ecom.E_Commerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ADMIN hierarchy: ADMIN hi SELLER/ADMIN/USER bana sakta hai
    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        String message = authService.createUserByAdmin(request);
        return ResponseEntity.ok(new ApiResponse(true, message));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<User>> getAllSellers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("SELLER"))
                .collect(Collectors.toList()));
    }

    @GetMapping("/sellers/{sellerId}/stats")
    public ResponseEntity<Map<String, Object>> getSellerStats(@PathVariable Long sellerId) {
        User seller = userRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Seller not found"));
        List<Product> products = productRepository.findBySellerId(sellerId);
        int totalProducts = products.size();
        int totalStock = products.stream().mapToInt(Product::getStock).sum();

        List<Order> allOrders = orderRepository.findAll();
        int pending = 0, completed = 0, totalSold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        Set<Long> orderIds = new HashSet<>();
        for (Order order : allOrders) {
            boolean hasSellerProduct = false;
            for (var item : order.getItems()) {
                if (item.getProduct().getSeller() != null && item.getProduct().getSeller().getId().equals(sellerId)) {
                    hasSellerProduct = true;
                    if (order.getStatus() != OrderStatus.CANCELLED) {
                        totalSold += item.getQuantity();
                        revenue = revenue.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                    break;
                }
            }
            if (hasSellerProduct) {
                orderIds.add(order.getId());
                if (order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.SHIPPED) pending++;
                else if (order.getStatus() == OrderStatus.DELIVERED) completed++;
                else if (order.getStatus() == OrderStatus.CANCELLED) completed++;
            }
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("sellerId", sellerId);
        stats.put("sellerName", seller.getName());
        stats.put("sellerEmail", seller.getEmail());
        stats.put("totalProducts", totalProducts);
        stats.put("totalStock", totalStock);
        stats.put("totalSold", totalSold);
        stats.put("totalRevenue", revenue);
        stats.put("totalOrders", orderIds.size());
        stats.put("pendingOrders", pending);
        stats.put("completedOrders", completed);
        // Stock details
        List<Map<String, Object>> stockDetails = products.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("stock", p.getStock());
            m.put("price", p.getPrice());
            m.put("isPublic", p.getIsPublic());
            m.put("category", p.getCategory().getName());
            return m;
        }).collect(Collectors.toList());
        stats.put("stockDetails", stockDetails);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<List<Product>> getSellerProducts(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productRepository.findBySellerId(sellerId));
    }

    @GetMapping("/sellers/{sellerId}/orders")
    public ResponseEntity<List<Map<String, Object>>> getSellerOrders(@PathVariable Long sellerId) {
        List<Order> allOrders = orderRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : allOrders) {
            List<Map<String, Object>> sellerItems = new ArrayList<>();
            for (var item : order.getItems()) {
                if (item.getProduct().getSeller() != null && item.getProduct().getSeller().getId().equals(sellerId)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productName", item.getProduct().getName());
                    m.put("quantity", item.getQuantity());
                    m.put("price", item.getPrice());
                    sellerItems.add(m);
                }
            }
            if (!sellerItems.isEmpty()) {
                Map<String, Object> o = new HashMap<>();
                o.put("orderId", order.getId());
                o.put("buyerEmail", order.getUser().getEmail());
                o.put("buyerName", order.getUser().getName());
                o.put("status", order.getStatus().name());
                o.put("totalAmount", order.getTotalAmount());
                o.put("shippingAddress", order.getShippingAddress());
                o.put("orderDate", order.getOrderDate());
                o.put("items", sellerItems);
                result.add(o);
            }
        }
        return ResponseEntity.ok(result);
    }
}
