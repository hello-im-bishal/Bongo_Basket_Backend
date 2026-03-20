package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;

@RestController
@CrossOrigin(origins = "https://hello-im-bishal.github.io") // Updated for flexibility
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create Razorpay Order
     * Now expects 'username' in the JSON body instead of relying on a Filter.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentOrder(@RequestBody Map<String, Object> requestBody) {
        try {
            // 1. Get username from request body
            String username = (String) requestBody.get("username");
            if (username == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
            }

            // 2. Fetch user from DB
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            // 3. Extract totalAmount (Total amount should be sent from frontend in Rupees)
            BigDecimal totalAmount = new BigDecimal(requestBody.get("totalAmount").toString());

            // 4. Create Razorpay order
            // Note: We don't necessarily need to pass cartItems here because 
            // the Service will fetch them from the DB during verification.
            String razorpayOrderId = paymentService.createOrder(user.getUserId(), totalAmount);

            return ResponseEntity.ok(Map.of("orderId", razorpayOrderId));
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Razorpay Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Verify Razorpay Payment
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> requestBody) {
        try {
            String username = (String) requestBody.get("username");
            String razorpayOrderId = (String) requestBody.get("razorpayOrderId");
            String razorpayPaymentId = (String) requestBody.get("razorpayPaymentId");
            String razorpaySignature = (String) requestBody.get("razorpaySignature");

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isVerified = paymentService.verifyPayment(
                    razorpayOrderId, 
                    razorpayPaymentId, 
                    razorpaySignature, 
                    user.getUserId()
            );

            if (isVerified) {
                return ResponseEntity.ok(Map.of("message", "Payment verified successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification Error: " + e.getMessage());
        }
    }
}