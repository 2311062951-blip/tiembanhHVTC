package com.hvtc.bakery.api;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentApiController {

    private final PayOS payOS;
    private final OrderService orderService;

    private User getUserOrNull(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }

    /**
     * API 1: Create Payment Link with PayOS (v2.0.1 SDK)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestBody(required = false) CheckoutForm form,
            @RequestParam(name = "orderId", required = false) Long existingOrderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            HttpServletRequest request
    ) {
        try {
            Order order;
            if (existingOrderId != null) {
                order = orderService.findById(existingOrderId);
            } else if (form != null) {
                form.setPaymentMethod(PaymentMethod.PAYOS);
                User user = getUserOrNull(userDetails);
                order = orderService.createOrder(user, session.getId(), form);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Thiếu thông tin đơn hàng để tạo thanh toán PayOS.");
                return ResponseEntity.badRequest().body(error);
            }

            // Build base URL
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);

            String returnUrl = baseUrl + "/checkout/success/" + order.getOrderCode();
            String cancelUrl = baseUrl + "/checkout";

            // Prepare items for PayOS
            List<PaymentLinkItem> items = new ArrayList<>();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    PaymentLinkItem itemData = PaymentLinkItem.builder()
                            .name(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice().longValue())
                            .build();
                    items.add(itemData);
                }
            }

            String description = "Don hang " + order.getId();
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }

            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(order.getId())
                    .amount(order.getGrandTotal().longValue())
                    .description(description)
                    .items(items)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .buyerName(order.getCustomerName())
                    .buyerEmail(order.getEmail())
                    .buyerPhone(order.getPhone())
                    .buyerAddress(order.getAddress())
                    .build();

            CreatePaymentLinkResponse checkoutResponse = payOS.paymentRequests().create(paymentRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("orderCode", order.getOrderCode());
            response.put("checkoutUrl", checkoutResponse.getCheckoutUrl());
            response.put("qrCode", checkoutResponse.getQrCode());
            response.put("bin", checkoutResponse.getBin());
            response.put("accountNumber", checkoutResponse.getAccountNumber());
            response.put("accountName", checkoutResponse.getAccountName());
            response.put("amount", checkoutResponse.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Lỗi khi tạo payment link PayOS: ", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * API 2: PayOS Webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody(required = false) Object webhookBody) {
        try {
            if (webhookBody != null) {
                try {
                    WebhookData webhookData = payOS.webhooks().verify(webhookBody);
                    if (webhookData != null && webhookData.getOrderCode() != null) {
                        Long orderId = webhookData.getOrderCode();
                        orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, PaymentStatus.PAID);
                        log.info("Xác thực Webhook PayOS thành công cho orderId={}", orderId);
                    }
                } catch (Exception ex) {
                    log.warn("Bypass lỗi verify Webhook (có thể là request test từ PayOS): {}", ex.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Webhook received and processed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.warn("Bypass lỗi tổng Webhook (trả về 200 OK để PayOS xác nhận URL thành công): {}", ex.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Webhook received");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * API 3: Check Payment Status by Order ID
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> checkStatus(@PathVariable("orderId") Long orderId) {
        try {
            Order order = orderService.findById(orderId);

            // Fallback check with PayOS if local status is still UNPAID
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                try {
                    PaymentLink payosLinkInfo = payOS.paymentRequests().get(orderId);
                    if (payosLinkInfo != null && payosLinkInfo.getStatus() != null && "PAID".equalsIgnoreCase(payosLinkInfo.getStatus().name())) {
                        order = orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING, PaymentStatus.PAID);
                    }
                } catch (Exception ignored) {
                    // PayOS API might fail or link not created yet; ignore fallback error
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("orderCode", order.getOrderCode());
            response.put("paymentStatus", order.getPaymentStatus().name());
            response.put("paymentStatusDisplayName", order.getPaymentStatus().getDisplayName());
            response.put("orderStatus", order.getOrderStatus().name());
            response.put("isPaid", order.getPaymentStatus() == PaymentStatus.PAID);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Lỗi khi kiểm tra trạng thái đơn hàng: ", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
