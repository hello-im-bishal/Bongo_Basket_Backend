package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "https://hello-im-bishal.github.io", allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category) {
        try {
            // 1. Handle category logic
            String categoryQuery = (category == null || category.equalsIgnoreCase("all") || category.isEmpty()) 
                                   ? null : category;
            
            List<Product> products = productService.getProductsByCategory(categoryQuery);

            // 2. Build the response object
            Map<String, Object> response = new HashMap<>();
            
            // Fallback User Info (Since Auth Filter is gone)
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", "Guest User"); 
            response.put("user", userInfo);
            response.put("username", "Guest");

            // 3. Map Products with their images
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> details = new HashMap<>();
                details.put("product_id", product.getProductId());
                details.put("name", product.getName());
                details.put("price", product.getPrice());
                details.put("description", product.getDescription());
                
                // Fetch image URLs via Service
                List<String> images = productService.getProductImages(product.getProductId());
                details.put("images", images); 
                
                productList.add(details);
            }
            response.put("products", productList);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}