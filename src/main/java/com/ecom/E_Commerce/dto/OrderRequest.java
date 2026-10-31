package com.ecom.E_Commerce.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderRequest {
    @NotBlank
    private String shippingAddress;

    private String paymentMethod = "COD";

    public OrderRequest() {}

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
