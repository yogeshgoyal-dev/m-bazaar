package com.ecom.E_Commerce.controller;

import com.ecom.E_Commerce.dto.ApiResponse;
import com.ecom.E_Commerce.model.Product;
import com.ecom.E_Commerce.repository.ProductRepository;
import com.ecom.E_Commerce.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ApiResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files allowed");
        }
        String path = fileStorageService.storeFile(file);
        product.setImageUrl(path);
        productRepository.save(product);
        return ResponseEntity.ok(new ApiResponse(true, "Image uploaded: " + path));
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ApiResponse> uploadImageGeneric(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        String path = fileStorageService.storeFile(file);
        return ResponseEntity.ok(new ApiResponse(true, path));
    }
}
