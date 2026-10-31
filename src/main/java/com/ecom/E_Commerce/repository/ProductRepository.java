package com.ecom.E_Commerce.repository;

import com.ecom.E_Commerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByIsPublicTrue();
    List<Product> findByCategoryIdAndIsPublicTrue(Long categoryId);
    List<Product> findByNameContainingIgnoreCaseAndIsPublicTrue(String keyword);
}
