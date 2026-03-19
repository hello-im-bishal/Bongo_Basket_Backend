package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.CartService;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5500", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    // GET /api/cart/items/count?username=...
    @GetMapping("/items/count")
    public ResponseEntity<Integer> getCartItemCount(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        int count = cartService.getCartItemCount(user.getUserId());
        return ResponseEntity.ok(count);
    }

    // GET /api/cart/items?username=...
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems(@RequestParam String username) {
        // Logic changed: Now receives username via Param instead of Request Attribute
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Map<String, Object> cartData = cartService.getCartItems(user.getUserId());
        return ResponseEntity.ok(cartData);
    }

    // POST /api/cart/add
    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int productId = (int) request.get("productId");
        int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        cartService.addToCart(user.getUserId(), productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // PUT /api/cart/update
    @PutMapping("/update")
    public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int productId = (int) request.get("productId");
        int quantity = (int) request.get("quantity");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/cart/delete
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int productId = (int) request.get("productId");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        cartService.deleteCartItem(user.getUserId(), productId);
        return ResponseEntity.noContent().build();
    }
}