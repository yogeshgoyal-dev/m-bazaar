package com.ecom.E_Commerce.service;

import com.ecom.E_Commerce.dto.JwtResponse;
import com.ecom.E_Commerce.dto.LoginRequest;
import com.ecom.E_Commerce.dto.RegisterRequest;
import com.ecom.E_Commerce.model.Role;
import com.ecom.E_Commerce.model.User;
import com.ecom.E_Commerce.repository.UserRepository;
import com.ecom.E_Commerce.security.JwtTokenProvider;
import com.ecom.E_Commerce.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        // Hierarchy: /api/auth/register se sirf USER (customer) banege, SELLER/ADMIN nahi
        if (request.getRole() != null && !request.getRole().equalsIgnoreCase("USER")) {
            throw new RuntimeException("Registration se sirf USER (customer) ban sakta hai! SELLER/ADMIN ke liye ADMIN se login karke /api/admin/users use karo");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return "User registered successfully as USER!";
    }

    // ADMIN hierarchy: ADMIN login karke hi SELLER/ADMIN bana sakta hai
    public String createUserByAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        Role role = Role.USER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role! Use USER, SELLER, ADMIN");
            }
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);
        return role.name() + " created successfully by ADMIN!";
    }

    // Public role upgrade/downgrade (abhi public, baad me ADMIN only karenge)
    public String updateRole(String email, String roleStr) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        try {
            Role newRole = Role.valueOf(roleStr.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return "Role updated to " + newRole.name() + " for " + email;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role! Use ADMIN, SELLER, USER");
        }
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return new JwtResponse(jwt, userPrincipal.getId(), userPrincipal.getName(),
                userPrincipal.getEmail(), userPrincipal.getAuthorities().iterator().next().getAuthority());
    }
}
