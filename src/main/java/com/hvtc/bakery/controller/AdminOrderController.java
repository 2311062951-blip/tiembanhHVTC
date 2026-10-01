package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Order;
import com.hvtc.bakery.entity.OrderStatus;
import com.hvtc.bakery.entity.PaymentStatus;
import com.hvtc.bakery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "15") int size,
            Model model
    ) {
        Page<Order> orderPage = orderService.findAllOrders(status, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("activeAdminMenu", "orders");
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("allOrderStatuses", OrderStatus.values());
        model.addAttribute("allPaymentStatuses", PaymentStatus.values());
        model.addAttribute("activeAdminMenu", "orders");
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam(name = "orderStatus", required = false) OrderStatus orderStatus,
            @RequestParam(name = "paymentStatus", required = false) PaymentStatus paymentStatus,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateOrderStatus(id, orderStatus, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
