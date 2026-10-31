package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.ApiResponse;
import com.ecom.E_Commerce.dto.CartRequest;
import com.ecom.E_Commerce.dto.CartResponse;
import com.ecom.E_Commerce.security.UserPrincipal;
import com.ecom.E_Commerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getEmail()));
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.addItem(principal.getEmail(), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateItem(@Valid @RequestBody CartRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.updateItem(principal.getEmail(), request.getProductId(), request.getQuantity()));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.removeItem(principal.getEmail(), productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Cart cleared"));
    }
}
