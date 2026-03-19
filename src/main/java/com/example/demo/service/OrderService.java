package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetches successful orders by looking up the user via username.
     */
    public Map<String, Object> getOrdersForUserByUsername(String username) {
        // 1. Find user in database since AuthFilter is gone
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Fetch all successful order items for this user ID
        List<OrderItem> orderItems = orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());

        // 3. Prepare Response
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        List<Map<String, Object>> products = new ArrayList<>();
        
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null) continue;

            // Fetch image (get first image or null)
            List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();

            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("order_id", item.getOrder().getOrderId());
            productDetails.put("product_id", product.getProductId());
            productDetails.put("name", product.getName());
            productDetails.put("description", product.getDescription());
            productDetails.put("quantity", item.getQuantity());
            productDetails.put("price_per_unit", item.getPricePerUnit());
            productDetails.put("total_price", item.getTotalPrice());
            productDetails.put("image_url", imageUrl);
            productDetails.put("order_date", item.getOrder().getCreatedAt());

            products.add(productDetails);
        }

        response.put("products", products);
        return response;
    }
}