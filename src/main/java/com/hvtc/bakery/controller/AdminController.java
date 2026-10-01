package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.DashboardStatsDto;
import com.hvtc.bakery.service.OrderService;
import com.hvtc.bakery.service.ProductService;
import com.hvtc.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String dashboard(Model model) {
        DashboardStatsDto stats = orderService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentOrders", orderService.findRecentOrders());
        model.addAttribute("activeAdminMenu", "dashboard");
        return "admin/dashboard";
    }
}
