package com.example.demo.service;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.User;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public int getCartItemCount(int userId) {
        return cartRepository.countTotalItems(userId);
    }

    @Transactional
    public void addToCart(int userId, int productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(userId, productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem(user, product, quantity);
            cartRepository.save(newItem);
        }
    }

    public Map<String, Object> getCartItems(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());

        List<Map<String, Object>> productsList = new ArrayList<>();
        double overallTotalPrice = 0;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            Map<String, Object> details = new HashMap<>();

            // Image handling
            List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = (images != null && !images.isEmpty()) 
                                ? images.get(0).getImageUrl() 
                                : "https://via.placeholder.com/150";

            double unitPrice = product.getPrice().doubleValue();
            double subTotal = item.getQuantity() * unitPrice;

            details.put("product_id", product.getProductId());
            details.put("image_url", imageUrl);
            details.put("name", product.getName());
            details.put("description", product.getDescription());
            details.put("price_per_unit", unitPrice);
            details.put("quantity", item.getQuantity());
            details.put("total_price", subTotal);

            productsList.add(details);
            overallTotalPrice += subTotal;
        }

        Map<String, Object> cartContent = new HashMap<>();
        cartContent.put("products", productsList);
        cartContent.put("overall_total_price", overallTotalPrice);
        
        response.put("cart", cartContent);
        return response;
    }

    @Transactional
    public void updateCartItemQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            deleteCartItem(userId, productId);
            return;
        }

        CartItem item = cartRepository.findByUserAndProduct(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        item.setQuantity(quantity);
        cartRepository.save(item);
    }

    @Transactional
    public void deleteCartItem(int userId, int productId) {
        cartRepository.deleteCartItem(userId, productId);
    }
}