package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.ApiResponse;
import com.ecom.E_Commerce.dto.ProductRequest;
import com.ecom.E_Commerce.dto.ProductResponse;
import com.ecom.E_Commerce.security.UserPrincipal;
import com.ecom.E_Commerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GUEST - public
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.getAll(categoryId, keyword));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.getAll(null, keyword));
    }

    // ADMIN + SELLER - create (GUEST nahi)
    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        String email = principal != null ? principal.getEmail() : null;
        return ResponseEntity.ok(productService.create(request, email));
    }

    // Legacy admin path also support
    @PostMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createAdmin(@Valid @RequestBody ProductRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        String email = principal != null ? principal.getEmail() : null;
        return ResponseEntity.ok(productService.create(request, email));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(productService.update(id, request, principal.getEmail(), isAdmin));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        productService.delete(id, principal.getEmail(), isAdmin);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }
}
