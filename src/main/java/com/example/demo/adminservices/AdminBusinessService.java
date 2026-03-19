package com.example.demo.adminservices;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminBusinessService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public AdminBusinessService(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    // =========================
    // MONTHLY BUSINESS
    // =========================
    public Map<String, Object> calculateMonthlyBusiness(Integer month, Integer year) {

        validateMonthYear(month, year);

        List<Order> successfulOrders =
                orderRepository.findSuccessfulOrdersByMonthAndYear(month, year);

        return buildBusinessReport(successfulOrders);
    }

    // =========================
    // DAILY BUSINESS
    // =========================
    public Map<String, Object> calculateDailyBusiness(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        List<Order> successfulOrders =
                orderRepository.findSuccessfulOrdersByDate(date);

        return buildBusinessReport(successfulOrders);
    }

    // =========================
    // YEARLY BUSINESS
    // =========================
    public Map<String, Object> calculateYearlyBusiness(Integer year) {

        if (year == null || year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }

        List<Order> successfulOrders =
                orderRepository.findSuccessfulOrdersByYear(year);

        return buildBusinessReport(successfulOrders);
    }

    // =========================
    // OVERALL BUSINESS
    // =========================
    public Map<String, Object> calculateOverallBusiness() {

        BigDecimal totalBusinessAmount =
                orderRepository.calculateOverallBusiness();

        if (totalBusinessAmount == null) {
            totalBusinessAmount = BigDecimal.ZERO;
        }

        List<Order> successfulOrders =
                orderRepository.findAllByStatus(OrderStatus.SUCCESS);

        Map<String, Integer> categorySales =
                calculateCategorySales(successfulOrders);

        Map<String, Object> response = new HashMap<>();
        response.put("totalBusiness", totalBusinessAmount);
        response.put("categorySales", categorySales);

        return response;
    }

    // =========================
    // COMMON REPORT BUILDER
    // =========================
    private Map<String, Object> buildBusinessReport(List<Order> orders) {

        BigDecimal totalBusiness = BigDecimal.ZERO;
        Map<String, Integer> categorySales =
                calculateCategorySales(orders);

        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalBusiness = totalBusiness.add(order.getTotalAmount());
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalBusiness", totalBusiness);
        report.put("categorySales", categorySales);

        return report;
    }

    // =========================
    // CATEGORY SALES CALCULATION
    // =========================
    private Map<String, Integer> calculateCategorySales(List<Order> orders) {

        Map<String, Integer> categorySales = new HashMap<>();

        for (Order order : orders) {

            List<OrderItem> items =
                    orderItemRepository.findByOrderId(order.getOrderId());

            for (OrderItem item : items) {

                String categoryName =
                        productRepository.findCategoryNameByProductId(item.getProductId());

                if (categoryName == null) {
                    categoryName = "Unknown";
                }

                categorySales.put(
                        categoryName,
                        categorySales.getOrDefault(categoryName, 0)
                                + item.getQuantity()
                );
            }
        }

        return categorySales;
    }

    // =========================
    // VALIDATION
    // =========================
    private void validateMonthYear(Integer month, Integer year) {

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }

        if (year == null || year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }
    }
}