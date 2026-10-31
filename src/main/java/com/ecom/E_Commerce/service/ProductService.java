package com.ecom.E_Commerce.service;

import com.ecom.E_Commerce.dto.ProductRequest;
import com.ecom.E_Commerce.dto.ProductResponse;
import com.ecom.E_Commerce.exception.ResourceNotFoundException;
import com.ecom.E_Commerce.model.Category;
import com.ecom.E_Commerce.model.Product;
import com.ecom.E_Commerce.model.User;
import com.ecom.E_Commerce.repository.CategoryRepository;
import com.ecom.E_Commerce.repository.ProductRepository;
import com.ecom.E_Commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(), p.getImageUrl(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getSeller() != null ? p.getSeller().getId() : null,
                p.getSeller() != null ? p.getSeller().getName() : null,
                p.getIsPublic(),
                p.getCreatedAt()
        );
    }

    public ProductResponse create(ProductRequest req, String sellerEmail) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
        User seller = null;
        if (sellerEmail != null) {
            seller = userRepository.findByEmail(sellerEmail).orElse(null);
        }
        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setImageUrl(req.getImageUrl());
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsPublic(req.getIsPublic() != null ? req.getIsPublic() : true);
        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAll(Long categoryId, String keyword) {
        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndIsPublicTrue(keyword);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndIsPublicTrue(categoryId);
        } else {
            products = productRepository.findByIsPublicTrue();
        }
        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllForAdmin() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(p);
    }

    public ProductResponse update(Long id, ProductRequest req, String currentUserEmail, boolean isAdmin) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        // SELLER can only update own product - ADMIN product (seller==null) ko SELLER edit nahi kar sakta
        if (!isAdmin) {
            if (p.getSeller() == null || !p.getSeller().getEmail().equals(currentUserEmail)) {
                throw new RuntimeException("You can only update your own products");
            }
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) p.setImageUrl(req.getImageUrl());
        p.setCategory(category);
        if (req.getIsPublic() != null) p.setIsPublic(req.getIsPublic());
        return toResponse(productRepository.save(p));
    }

    public void delete(Long id, String currentUserEmail, boolean isAdmin) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (!isAdmin) {
            if (p.getSeller() == null || !p.getSeller().getEmail().equals(currentUserEmail)) {
                throw new RuntimeException("You can only delete your own products");
            }
        }
        productRepository.delete(p);
    }
}
