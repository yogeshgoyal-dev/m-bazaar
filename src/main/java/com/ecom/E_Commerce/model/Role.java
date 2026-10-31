package com.ecom.E_Commerce.model;

public enum Role {
    ADMIN,   // Official Company - sab control (users, categories, all products, all orders)
    SELLER,  // Seller - apne products manage, apne orders dekhe
    USER     // Buyer - kharid sake, cart/order
    // GUEST - bina login, sirf dekh sakta hai (DB me save nahi, permitAll)
}
