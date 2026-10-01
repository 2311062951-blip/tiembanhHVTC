package com.hvtc.bakery.controller;

import com.hvtc.bakery.service.BlogService;
import com.hvtc.bakery.service.CategoryService;
import com.hvtc.bakery.service.ProductService;
import com.hvtc.bakery.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BlogService blogService;
    private final WorkshopService workshopService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("featuredProducts", productService.findFeaturedProducts());
        model.addAttribute("bestSellers", productService.findBestSellers());
        model.addAttribute("birthdayCakes", productService.findByCategory("banh-sinh-nhat"));
        model.addAttribute("recentPosts", blogService.findRecentPosts());
        model.addAttribute("workshops", workshopService.findAllActiveWorkshops());
        model.addAttribute("activeMenu", "home");
        return "home/index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activeMenu", "about");
        return "about/about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activeMenu", "contact");
        return "contact/contact";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", productService.searchByName(keyword));
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("activeMenu", "products");
        return "product/list";
    }
}
