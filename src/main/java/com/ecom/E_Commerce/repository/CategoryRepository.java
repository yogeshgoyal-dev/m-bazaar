package com.ecom.E_Commerce.repository;

import com.ecom.E_Commerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Boolean existsByName(String name);
    List<Category> findByParentIsNull();
    List<Category> findByParentId(Long parentId);
}
