package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.BlogPost;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BlogService {
    List<BlogPost> findRecentPosts();
    Page<BlogPost> findAllPosts(int page, int size, String keyword);
    BlogPost findBySlug(String slug);
    BlogPost findById(Long id);
    BlogPost savePost(BlogPost post);
    void deletePost(Long id);
    List<BlogPost> findAll();
}
