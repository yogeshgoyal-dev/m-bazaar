package com.ecom.E_Commerce.service;

import com.ecom.E_Commerce.dto.CartResponse;
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
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart(user);
                    return cartRepository.save(cart);
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            Product p = item.getProduct();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartResponse.CartItemResponse(
                    item.getId(), p.getId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal, p.getImageUrl()
            );
        }).collect(Collectors.toList());
        BigDecimal total = itemResponses.stream().map(CartResponse.CartItemResponse::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = itemResponses.stream().mapToInt(CartResponse.CartItemResponse::getQuantity).sum();
        return new CartResponse(cart.getId(), itemResponses, total, totalItems);
    }

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return toResponse(cart);
    }

    public CartResponse addItem(String email, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }
        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (product.getStock() < newQty) throw new RuntimeException("Insufficient stock for total quantity");
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem(cart, product, quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
        cart = cartRepository.findById(cart.getId()).orElse(cart);
        return toResponse(cart);
    }

    public CartResponse updateItem(String email, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));
        Product product = item.getProduct();
        if (product.getStock() < quantity) throw new RuntimeException("Insufficient stock");
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    public CartResponse removeItem(String email, Long productId) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
