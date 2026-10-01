package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.BlogPost;
import com.hvtc.bakery.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/blog")
    public String listBlogPosts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Model model
    ) {
        Page<BlogPost> postPage = blogService.findAllPosts(page, size, keyword);
        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("recentPosts", blogService.findRecentPosts());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "blog");
        return "blog/list";
    }

    @GetMapping("/blog/{slug}")
    public String blogDetail(@PathVariable("slug") String slug, Model model) {
        BlogPost post = blogService.findBySlug(slug);
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", blogService.findRecentPosts());
        model.addAttribute("activeMenu", "blog");
        return "blog/detail";
    }
}
