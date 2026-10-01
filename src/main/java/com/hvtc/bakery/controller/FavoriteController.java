package com.hvtc.bakery.controller;

import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/add/{productId}")
    public String addFavorite(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            @RequestHeader(name = "Referer", required = false) String referer
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        favoriteService.addFavorite(userDetails.getId(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm bánh vào danh sách yêu thích!");
        return "redirect:" + (referer != null ? referer : "/account/favorites");
    }

    @PostMapping("/remove/{productId}")
    public String removeFavorite(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            @RequestHeader(name = "Referer", required = false) String referer
    ) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        favoriteService.removeFavorite(userDetails.getId(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khỏi danh sách yêu thích!");
        return "redirect:" + (referer != null ? referer : "/account/favorites");
    }
}
