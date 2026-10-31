package com.ecom.E_Commerce.service;

import com.ecom.E_Commerce.dto.CategoryRequest;
import com.ecom.E_Commerce.dto.CategoryResponse;
import com.ecom.E_Commerce.exception.ResourceNotFoundException;
import com.ecom.E_Commerce.model.Category;
import com.ecom.E_Commerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryResponse toResponse(Category cat) {
        Long parentId = cat.getParent() != null ? cat.getParent().getId() : null;
        String parentName = cat.getParent() != null ? cat.getParent().getName() : null;
        return new CategoryResponse(cat.getId(), cat.getName(), cat.getDescription(), parentId, parentName, cat.getCreatedAt());
    }

    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByName(req.getName())) {
            throw new RuntimeException("Category already exists with name: " + req.getName());
        }
        Category parent = null;
        if (req.getParentId() != null) {
            parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + req.getParentId()));
        }
        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setParent(parent);
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CategoryResponse getById(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toResponse(cat);
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (!cat.getName().equals(req.getName()) && categoryRepository.existsByName(req.getName())) {
            throw new RuntimeException("Category already exists with name: " + req.getName());
        }
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        if (req.getParentId() != null) {
            Category parent = categoryRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            if (parent.getId().equals(id)) throw new RuntimeException("Category cannot be parent of itself");
            cat.setParent(parent);
        } else {
            cat.setParent(null);
        }
        return toResponse(categoryRepository.save(cat));
    }

    public void delete(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        // Check if has children
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("Cannot delete category with sub-categories. Delete sub-categories first.");
        }
        categoryRepository.delete(cat);
    }
}
