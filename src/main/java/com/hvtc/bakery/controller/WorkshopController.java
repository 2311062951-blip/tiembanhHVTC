package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.entity.Workshop;
import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping("/workshops")
    public String listWorkshops(Model model) {
        model.addAttribute("workshops", workshopService.findAllActiveWorkshops());
        model.addAttribute("activeMenu", "workshop");
        return "workshop/list";
    }

    @GetMapping("/workshop/{slug}")
    public String workshopDetail(@PathVariable("slug") String slug, Model model) {
        Workshop workshop = workshopService.findBySlug(slug);
        model.addAttribute("workshop", workshop);
        model.addAttribute("activeMenu", "workshop");
        return "workshop/detail";
    }

    @PostMapping("/workshop/register")
    public String registerWorkshop(
            @RequestParam("workshopId") Long workshopId,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam(name = "note", required = false) String note,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userDetails != null ? userDetails.getUser() : null;
            workshopService.registerWorkshop(workshopId, user, name, phone, email, note);
            Workshop workshop = workshopService.findById(workshopId);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tham gia workshop thành công! HVTC BAKERY sẽ liên hệ xác nhận sớm nhất.");
            return "redirect:/workshop/" + workshop.getSlug();
        } catch (Exception ex) {
            Workshop workshop = workshopService.findById(workshopId);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workshop/" + workshop.getSlug();
        }
    }
}
