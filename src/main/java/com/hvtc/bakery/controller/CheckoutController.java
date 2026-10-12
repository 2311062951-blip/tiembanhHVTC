package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CartService;
import com.hvtc.bakery.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PayOS payOS;

    private User getUserOrNull(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }

    @GetMapping
    public String showCheckoutPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            Model model
    ) {
        User user = getUserOrNull(userDetails);
        Cart cart = cartService.getCartForUserOrSession(user, session.getId());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutForm form = new CheckoutForm();
        if (user != null) {
            form.setCustomerName(user.getFullName());
            form.setEmail(user.getEmail());
            form.setPhone(user.getPhone());
            form.setAddress(user.getAddress());
        }
        form.setPaymentMethod(PaymentMethod.PAYOS);

        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", form);
        model.addAttribute("activeMenu", "cart");

        return "checkout/checkout";
    }

    @PostMapping("/submit")
    public String processCheckout(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = getUserOrNull(userDetails);
        Cart cart = cartService.getCartForUserOrSession(user, session.getId());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống!");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("activeMenu", "cart");
            return "checkout/checkout";
        }

        try {
            Order order = orderService.createOrder(user, session.getId(), form);

            // If PayOS is selected, redirect to PayOS payment link instead of direct success page
            if (form.getPaymentMethod() == PaymentMethod.PAYOS) {
                try {
                    List<PaymentLinkItem> items = new ArrayList<>();
                    if (order.getItems() != null) {
                        for (OrderItem item : order.getItems()) {
                            items.add(PaymentLinkItem.builder()
                                    .name(item.getProduct().getName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice().longValue())
                                    .build());
                        }
                    }

                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();
                    String baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);

                    String description = "Don hang " + order.getId();
                    if (description.length() > 25) {
                        description = description.substring(0, 25);
                    }

                    CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                            .orderCode(order.getId())
                            .amount(order.getGrandTotal().longValue())
                            .description(description)
                            .items(items)
                            .returnUrl(baseUrl + "/checkout/success/" + order.getOrderCode())
                            .cancelUrl(baseUrl + "/checkout")
                            .buyerName(order.getCustomerName())
                            .buyerEmail(order.getEmail())
                            .buyerPhone(order.getPhone())
                            .buyerAddress(order.getAddress())
                            .build();

                    CreatePaymentLinkResponse checkoutResponseData = payOS.paymentRequests().create(paymentRequest);
                    return "redirect:" + checkoutResponseData.getCheckoutUrl();
                } catch (Exception ex) {
                    log.error("Lỗi khi tạo PayOS link trong CheckoutController: ", ex);
                }
            }

            return "redirect:/checkout/success/" + order.getOrderCode();
        } catch (Exception ex) {
            model.addAttribute("cart", cart);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeMenu", "cart");
            return "checkout/checkout";
        }
    }

    @GetMapping("/success/{orderCode}")
    public String orderSuccess(@PathVariable("orderCode") String orderCode, Model model) {
        Order order = orderService.findByOrderCode(orderCode);
        model.addAttribute("order", order);
        return "checkout/success";
    }
}
