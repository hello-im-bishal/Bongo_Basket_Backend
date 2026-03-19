package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getProductsByCategory(String categoryName) {
        // If no category is provided, return all products
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return productRepository.findAll();
        }

        // Search for category by name
        Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
        
        if (categoryOpt.isPresent()) {
            return productRepository.findByCategory_CategoryId(categoryOpt.get().getCategoryId());
        } else {
            // Instead of crashing, return an empty list or handle as needed
            return new ArrayList<>(); 
        }
    }

    public List<String> getProductImages(Integer productId) {
        // Using Java Streams for cleaner code
        return productImageRepository.findByProduct_ProductId(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
}