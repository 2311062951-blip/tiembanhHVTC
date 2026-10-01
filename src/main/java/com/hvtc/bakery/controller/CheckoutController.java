package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.Order;
import com.hvtc.bakery.entity.PaymentMethod;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CartService;
import com.hvtc.bakery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

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
        form.setPaymentMethod(PaymentMethod.COD);

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
