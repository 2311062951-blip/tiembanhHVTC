package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;

    @ModelAttribute("cartCount")
    public int getCartCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session
    ) {
        if (session == null) {
            return 0;
        }
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            Cart cart = cartService.getCartForUserOrSession(user, session.getId());
            int count = (cart != null) ? cart.getTotalQuantity() : 0;
            session.setAttribute("cartCount", count);
            return count;
        } catch (Exception ex) {
            return 0;
        }
    }
}
