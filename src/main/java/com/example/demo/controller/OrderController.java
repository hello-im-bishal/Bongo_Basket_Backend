package com.example.demo.controller;

import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true") 
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Fetches all successful orders using a username query parameter.
     * URL: /api/orders?username=someUser
     */
    @GetMapping
    public ResponseEntity<?> getOrdersForUser(@RequestParam String username) {
        try {
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Username parameter is missing"));
            }

            // Fetch orders via the service layer
            Map<String, Object> response = orderService.getOrdersForUserByUsername(username);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Specifically handle case where user is not found
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred"));
        }
    }
}