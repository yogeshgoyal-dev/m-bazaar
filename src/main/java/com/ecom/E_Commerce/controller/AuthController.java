package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.ApiResponse;
import com.ecom.E_Commerce.dto.JwtResponse;
import com.ecom.E_Commerce.dto.LoginRequest;
import com.ecom.E_Commerce.dto.RegisterRequest;
import com.ecom.E_Commerce.dto.RoleUpdateRequest;
import com.ecom.E_Commerce.security.UserPrincipal;
import com.ecom.E_Commerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(new ApiResponse(true, message));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Not authenticated"));
        }
        return ResponseEntity.ok(new JwtResponse("", userPrincipal.getId(), userPrincipal.getName(),
                userPrincipal.getEmail(), userPrincipal.getAuthorities().iterator().next().getAuthority()));
    }

    // ADMIN only - role upgrade/downgrade (pehle public tha, ab protect kiya)
    @PutMapping("/role")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateRole(@Valid @RequestBody RoleUpdateRequest request) {
        String message = authService.updateRole(request.getEmail(), request.getRole());
        return ResponseEntity.ok(new ApiResponse(true, message));
    }
}
