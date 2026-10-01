package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.CategoryDto;
import com.hvtc.bakery.entity.Category;
import com.hvtc.bakery.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("activeAdminMenu", "categories");
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categoryDto", new CategoryDto());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeAdminMenu", "categories");
        return "admin/categories/form";
    }

    @PostMapping("/create")
    public String saveCategory(
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activeAdminMenu", "categories");
            return "admin/categories/form";
        }

        try {
            categoryService.saveCategory(categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục mới thành công!");
            return "redirect:/admin/categories";
        } catch (Exception ex) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "categories");
            return "admin/categories/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);

        CategoryDto dto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .image(category.getImage())
                .status(category.isStatus())
                .build();

        model.addAttribute("categoryDto", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeAdminMenu", "categories");
        return "admin/categories/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activeAdminMenu", "categories");
            return "admin/categories/form";
        }

        try {
            categoryService.updateCategory(id, categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            return "redirect:/admin/categories";
        } catch (Exception ex) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "categories");
            return "admin/categories/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
