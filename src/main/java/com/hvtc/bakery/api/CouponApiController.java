package com.hvtc.bakery.api;

import com.hvtc.bakery.dto.CouponApplyRequest;
import com.hvtc.bakery.dto.CouponApplyResponse;
import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CartService;
import com.hvtc.bakery.service.CouponService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponApiController {

    private final CouponService couponService;
    private final CartService cartService;

    @PostMapping("/apply")
    public ResponseEntity<CouponApplyResponse> applyCoupon(
            @RequestBody CouponApplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpSession session
    ) {
        User user = userDetails != null ? userDetails.getUser() : null;
        Cart cart = cartService.getCartForUserOrSession(user, session.getId());

        BigDecimal cartTotal = (request != null && request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0)
                ? request.getAmount()
                : (cart != null ? cart.getTotalAmount() : BigDecimal.ZERO);

        String code = request != null ? request.getCode() : null;
        CouponApplyResponse response = couponService.validateAndApplyCoupon(code, cartTotal);
        return ResponseEntity.ok(response);
    }
}
