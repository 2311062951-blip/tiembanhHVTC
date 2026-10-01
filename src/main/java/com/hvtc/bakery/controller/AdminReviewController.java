package com.hvtc.bakery.controller;

import com.hvtc.bakery.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewService.findAllReviews());
        model.addAttribute("activeAdminMenu", "reviews");
        return "admin/reviews/list";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleReviewStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        reviewService.toggleReviewStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái hiển thị đánh giá thành công!");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa đánh giá thành công!");
        return "redirect:/admin/reviews";
    }
}
