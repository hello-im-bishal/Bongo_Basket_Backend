package com.example.demo.adminservices;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class AdminOrderService {

    private final JdbcTemplate jdbcTemplate;

    public AdminOrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getAllOrders() {
        String sql = "SELECT order_id, user_id, total_amount, status FROM orders ORDER BY order_id DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Transactional
    public void deleteOrder(String orderId) {
        // Delete items first to satisfy foreign key constraints
        jdbcTemplate.update("DELETE FROM order_items WHERE order_id = ?", orderId);
        // Delete the main order
        jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", orderId);
    }
}