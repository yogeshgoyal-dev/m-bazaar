package com.ecom.E_Commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RoleUpdateRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role; // ADMIN, SELLER, USER

    public RoleUpdateRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
