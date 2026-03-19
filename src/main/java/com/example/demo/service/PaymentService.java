package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public PaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public String createOrder(int userId, BigDecimal totalAmount) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Razorpay expects amount in PAISA (1 Rupee = 100 Paisa)
        int amountInPaisa = totalAmount.multiply(new BigDecimal(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaisa);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        // Save the pending order in our database
        Order order = new Order();
        order.setOrderId(razorpayOrderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        orderRepository.save(order);

        return razorpayOrderId;
    }

    @Transactional
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, int userId) {
        try {
            // 1. Verify Signature
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isSignatureValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (isSignatureValid) {
                // 2. Update Order Status
                Order order = orderRepository.findById(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Order not found"));
                
                order.setStatus(OrderStatus.SUCCESS);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                // 3. Move Cart Items to Order Items
                // Assumes your cartRepository has this custom query or method
                List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

                for (CartItem item : cartItems) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(item.getProduct().getProductId());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setPricePerUnit(item.getProduct().getPrice());
                    orderItem.setTotalPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    
                    orderItemRepository.save(orderItem);
                }

                // 4. Clear the Cart
                cartRepository.deleteAllCartItemsByUserId(userId);

                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}