package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.dto.DashboardStatsDto;
import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.OrderRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.CartService;
import com.hvtc.bakery.service.OrderService;
import com.hvtc.bakery.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.hvtc.bakery.dto.CouponApplyResponse;
import com.hvtc.bakery.entity.Coupon;
import com.hvtc.bakery.service.CouponService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final CouponService couponService;

    @Override
    @Transactional
    public Order createOrder(User user, String sessionId, CheckoutForm form) {
        Cart cart = cartService.getCartForUserOrSession(user, sessionId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống! Vui lòng chọn bánh trước khi đặt hàng.");
        }

        // Validate stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Sản phẩm bánh '" + product.getName() + "' không đủ tồn kho (chỉ còn " + product.getStockQuantity() + " bánh)");
            }
        }

        String orderCode = CodeGenerator.generateOrderCode();
        BigDecimal totalAmount = cart.getTotalAmount();
        BigDecimal shippingFee = BigDecimal.ZERO;

        String fullAddress = form.getAddress();
        if (form.getWard() != null && !form.getWard().isBlank()) {
            fullAddress += ", " + form.getWard();
        }
        if (form.getDistrict() != null && !form.getDistrict().isBlank()) {
            fullAddress += ", " + form.getDistrict();
        }
        if (form.getProvince() != null && !form.getProvince().isBlank()) {
            fullAddress += ", " + form.getProvince();
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        if (form.getCouponCode() != null && !form.getCouponCode().isBlank()) {
            CouponApplyResponse couponResp = couponService.validateAndApplyCoupon(form.getCouponCode(), totalAmount);
            if (couponResp.isSuccess()) {
                discountAmount = couponResp.getDiscountAmount();
                appliedCouponCode = couponResp.getCode();
                try {
                    Coupon coupon = couponService.findByCode(appliedCouponCode);
                    coupon.setUsedCount(coupon.getUsedCount() + 1);
                } catch (Exception ignored) {}
            }
        }

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .customerName(form.getCustomerName().trim())
                .phone(form.getPhone().trim())
                .email(form.getEmail().trim())
                .address(fullAddress)
                .note(form.getNote())
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .couponCode(appliedCouponCode)
                .discountAmount(discountAmount)
                .paymentMethod(form.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal price = product.getEffectivePrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByUser(User user) {
        if (user == null) return List.of();
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByOrderStatus(status, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status, PaymentStatus paymentStatus) {
        Order order = findById(orderId);
        OrderStatus oldStatus = order.getOrderStatus();

        if (status != null) {
            order.setOrderStatus(status);

            // If changing to CANCELLED and was not cancelled before, restore stock
            if (status == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        if (paymentStatus != null) {
            order.setPaymentStatus(paymentStatus);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;

        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long lowStock = productRepository.countByStockQuantityLessThan(5);

        return DashboardStatsDto.builder()
                .totalRevenue(revenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .pendingOrdersCount(pendingOrders)
                .lowStockProductsCount(lowStock)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void deleteAllOrders() {
        orderRepository.deleteAll();
    }
}
