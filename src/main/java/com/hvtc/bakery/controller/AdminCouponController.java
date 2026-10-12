package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.CouponDto;
import com.hvtc.bakery.entity.Coupon;
import com.hvtc.bakery.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.findAllCoupons());
        model.addAttribute("activeAdminMenu", "coupons");
        return "admin/coupons/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("couponDto", new CouponDto());
        model.addAttribute("activeAdminMenu", "coupons");
        model.addAttribute("isEdit", false);
        return "admin/coupons/form";
    }

    @PostMapping("/create")
    public String createCoupon(
            @Valid @ModelAttribute("couponDto") CouponDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeAdminMenu", "coupons");
            model.addAttribute("isEdit", false);
            return "admin/coupons/form";
        }

        try {
            couponService.saveCoupon(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã giảm giá mới thành công!");
            return "redirect:/admin/coupons";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "coupons");
            model.addAttribute("isEdit", false);
            return "admin/coupons/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Coupon coupon = couponService.findById(id);
        CouponDto dto = CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercent(coupon.getDiscountPercent())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .expirationDate(coupon.getExpirationDate())
                .usageLimit(coupon.getUsageLimit())
                .active(coupon.isActive())
                .build();

        model.addAttribute("couponDto", dto);
        model.addAttribute("activeAdminMenu", "coupons");
        model.addAttribute("isEdit", true);
        return "admin/coupons/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCoupon(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("couponDto") CouponDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeAdminMenu", "coupons");
            model.addAttribute("isEdit", true);
            return "admin/coupons/form";
        }

        try {
            couponService.updateCoupon(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
            return "redirect:/admin/coupons";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "coupons");
            model.addAttribute("isEdit", true);
            return "admin/coupons/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleCouponStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái mã giảm giá thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}
