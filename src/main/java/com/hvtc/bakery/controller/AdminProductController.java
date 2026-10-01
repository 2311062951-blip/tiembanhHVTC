package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.ProductDto;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.service.CategoryService;
import com.hvtc.bakery.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("activeAdminMenu", "products");
        return "admin/products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeAdminMenu", "products");
        return "admin/products/form";
    }

    @PostMapping("/create")
    public String saveProduct(
            @Valid @ModelAttribute("productDto") ProductDto productDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeAdminMenu", "products");
            return "admin/products/form";
        }

        try {
            productService.saveProduct(productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới bánh thành công!");
            return "redirect:/admin/products";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "products");
            return "admin/products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);

        ProductDto dto = ProductDto.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .stockQuantity(product.getStockQuantity())
                .image(product.getImage())
                .categoryId(product.getCategory().getId())
                .status(product.isStatus())
                .featured(product.isFeatured())
                .bestSeller(product.isBestSeller())
                .build();

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeAdminMenu", "products");
        return "admin/products/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("productDto") ProductDto productDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeAdminMenu", "products");
            return "admin/products/form";
        }

        try {
            productService.updateProduct(id, productDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm bánh thành công!");
            return "redirect:/admin/products";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeAdminMenu", "products");
            return "admin/products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bánh thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.toggleFeatured(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái nổi bật!");
        return "redirect:/admin/products";
    }

    @PostMapping("/toggle-bestseller/{id}")
    public String toggleBestSeller(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.toggleBestSeller(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái bán chạy!");
        return "redirect:/admin/products";
    }
}
