package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.UserUpdateDto;
import com.hvtc.bakery.entity.Order;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.FavoriteService;
import com.hvtc.bakery.service.OrderService;
import com.hvtc.bakery.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;
    private final FavoriteService favoriteService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());

        UserUpdateDto dto = UserUpdateDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();

        model.addAttribute("user", user);
        model.addAttribute("userUpdateDto", dto);
        model.addAttribute("activeTab", "profile");
        return "account/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("userUpdateDto") UserUpdateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            User user = userService.findById(userDetails.getId());
            model.addAttribute("user", user);
            model.addAttribute("activeTab", "profile");
            return "account/profile";
        }

        try {
            userService.updateProfile(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
            return "redirect:/account/profile";
        } catch (Exception ex) {
            User user = userService.findById(userDetails.getId());
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeTab", "profile");
            return "account/profile";
        }
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.findOrdersByUser(user));
        model.addAttribute("activeTab", "orders");
        return "account/orders";
    }

    @GetMapping("/order/{code}")
    public String orderDetail(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Order order = orderService.findByOrderCode(code);
        if (order.getUser() != null && !order.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/account/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("activeTab", "orders");
        return "account/order_detail";
    }

    @GetMapping("/favorites")
    public String favorites(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("favorites", favoriteService.getUserFavorites(userDetails.getId()));
        model.addAttribute("activeTab", "favorites");
        return "account/favorites";
    }
}
