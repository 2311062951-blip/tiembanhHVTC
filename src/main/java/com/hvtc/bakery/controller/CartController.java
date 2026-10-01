package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private User getUserOrNull(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }

    @GetMapping
    public String viewCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            Model model
    ) {
        User user = getUserOrNull(userDetails);
        Cart cart = cartService.getCartForUserOrSession(user, session.getId());

        model.addAttribute("cart", cart);
        model.addAttribute("activeMenu", "cart");
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getUserOrNull(userDetails);
            cartService.addToCart(user, session.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm bánh vào giỏ hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") int quantity,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getUserOrNull(userDetails);
            cartService.updateQuantity(user, session.getId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giỏ hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam("cartItemId") Long cartItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getUserOrNull(userDetails);
            cartService.removeFromCart(user, session.getId(), cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bánh khỏi giỏ hàng!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }
}
