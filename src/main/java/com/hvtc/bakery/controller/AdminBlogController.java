package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.BlogPost;
import com.hvtc.bakery.service.BlogService;
import com.hvtc.bakery.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listBlog(Model model) {
        model.addAttribute("posts", blogService.findAll());
        model.addAttribute("activeAdminMenu", "blog");
        return "admin/blog/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("post", new BlogPost());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeAdminMenu", "blog");
        return "admin/blog/form";
    }

    @PostMapping("/create")
    public String savePost(
            @ModelAttribute("post") BlogPost post,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile, "blog");
            post.setImage(imagePath);
        }
        blogService.savePost(post);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm bài viết mới thành công!");
        return "redirect:/admin/blog";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        BlogPost post = blogService.findById(id);
        model.addAttribute("post", post);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeAdminMenu", "blog");
        return "admin/blog/form";
    }

    @PostMapping("/edit/{id}")
    public String updatePost(
            @PathVariable("id") Long id,
            @ModelAttribute("post") BlogPost updatedPost,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        BlogPost post = blogService.findById(id);
        post.setTitle(updatedPost.getTitle());
        post.setSummary(updatedPost.getSummary());
        post.setContent(updatedPost.getContent());
        post.setAuthor(updatedPost.getAuthor());
        post.setStatus(updatedPost.isStatus());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile, "blog");
            post.setImage(imagePath);
        }

        blogService.savePost(post);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bài viết thành công!");
        return "redirect:/admin/blog";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        blogService.deletePost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa bài viết thành công!");
        return "redirect:/admin/blog";
    }
}
