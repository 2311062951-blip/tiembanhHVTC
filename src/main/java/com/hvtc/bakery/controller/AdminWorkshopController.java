package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Workshop;
import com.hvtc.bakery.service.FileStorageService;
import com.hvtc.bakery.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/workshops")
@RequiredArgsConstructor
public class AdminWorkshopController {

    private final WorkshopService workshopService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listWorkshops(Model model) {
        model.addAttribute("workshops", workshopService.findAllWorkshops());
        model.addAttribute("activeAdminMenu", "workshops");
        return "admin/workshops/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("workshop", new Workshop());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeAdminMenu", "workshops");
        return "admin/workshops/form";
    }

    @PostMapping("/create")
    public String saveWorkshop(
            @ModelAttribute("workshop") Workshop workshop,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile, "workshops");
            workshop.setImage(imagePath);
        }
        workshopService.saveWorkshop(workshop);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm workshop thành công!");
        return "redirect:/admin/workshops";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Workshop workshop = workshopService.findById(id);
        model.addAttribute("workshop", workshop);
        model.addAttribute("isEdit", true);
        model.addAttribute("registrations", workshopService.getRegistrationsForWorkshop(id));
        model.addAttribute("activeAdminMenu", "workshops");
        return "admin/workshops/form";
    }

    @PostMapping("/edit/{id}")
    public String updateWorkshop(
            @PathVariable("id") Long id,
            @ModelAttribute("workshop") Workshop updated,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        Workshop workshop = workshopService.findById(id);
        workshop.setName(updated.getName());
        workshop.setDescription(updated.getDescription());
        workshop.setPrice(updated.getPrice());
        workshop.setDuration(updated.getDuration());
        workshop.setCapacity(updated.getCapacity());
        workshop.setLocation(updated.getLocation());
        workshop.setWorkshopDate(updated.getWorkshopDate());
        workshop.setStatus(updated.isStatus());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile, "workshops");
            workshop.setImage(imagePath);
        }

        workshopService.saveWorkshop(workshop);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật workshop thành công!");
        return "redirect:/admin/workshops";
    }

    @PostMapping("/delete/{id}")
    public String deleteWorkshop(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        workshopService.deleteWorkshop(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa workshop thành công!");
        return "redirect:/admin/workshops";
    }
}
