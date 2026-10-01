package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Category;
import com.hvtc.bakery.entity.Product;

import com.hvtc.bakery.security.CustomUserDetails;
import com.hvtc.bakery.service.CategoryService;
import com.hvtc.bakery.service.FavoriteService;
import com.hvtc.bakery.service.ProductService;
import com.hvtc.bakery.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", required = false, defaultValue = "default") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model
    ) {
        Page<Product> productPage = productService.filterProducts(keyword, categorySlug, minPrice, maxPrice, sort, page, size);
        List<Category> categories = categoryService.findAllActive();

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categorySlug);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sort);
        model.addAttribute("activeMenu", "products");

        return "product/list";
    }

    @GetMapping("/category/{slug}")
    public String categoryProducts(@PathVariable("slug") String slug, Model model) {
        Category category = categoryService.findBySlug(slug);
        return listProducts(null, slug, null, null, "default", 0, 12, model);
    }

    @GetMapping("/product/{slug}")
    public String productDetail(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Product product = productService.findBySlug(slug);
        List<Product> relatedProducts = productService.findByCategory(product.getCategory().getSlug())
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4)
                .toList();

        boolean isFavorite = false;
        if (userDetails != null) {
            isFavorite = favoriteService.isFavorite(userDetails.getId(), product.getId());
        }

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("reviews", reviewService.getProductReviews(product.getId()));
        model.addAttribute("avgRating", reviewService.getAverageRating(product.getId()));
        model.addAttribute("reviewCount", reviewService.getReviewCount(product.getId()));
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("activeMenu", "products");

        return "product/detail";
    }

    @PostMapping("/review/add")
    public String addReview(
            @RequestParam("productId") Long productId,
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            reviewService.addReview(userDetails.getId(), productId, rating, comment);
            Product product = productService.findById(productId);
            return "redirect:/product/" + product.getSlug() + "#reviews";
        }
        return "redirect:/login";
    }
}
