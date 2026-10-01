package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.dto.DashboardStatsDto;
import com.hvtc.bakery.entity.Order;
import com.hvtc.bakery.entity.OrderStatus;
import com.hvtc.bakery.entity.PaymentStatus;
import com.hvtc.bakery.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order createOrder(User user, String sessionId, CheckoutForm form);
    Order findByOrderCode(String orderCode);
    Order findById(Long id);
    List<Order> findOrdersByUser(User user);
    Page<Order> findAllOrders(OrderStatus status, Pageable pageable);
    Order updateOrderStatus(Long orderId, OrderStatus status, PaymentStatus paymentStatus);
    DashboardStatsDto getDashboardStats();
    List<Order> findRecentOrders();
}
