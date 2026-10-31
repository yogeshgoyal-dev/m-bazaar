package com.ecom.E_Commerce.repository;

import com.ecom.E_Commerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserEmail(String email);
}
