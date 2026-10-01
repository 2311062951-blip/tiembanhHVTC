package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.entity.BlogPost;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.BlogPostRepository;
import com.hvtc.bakery.service.BlogService;
import com.hvtc.bakery.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogPostRepository blogPostRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findRecentPosts() {
        return blogPostRepository.findTop6ByStatusTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogPost> findAllPosts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword != null && !keyword.isBlank()) {
            return blogPostRepository.findByStatusTrueAndTitleContainingIgnoreCase(keyword.trim(), pageable);
        }
        return blogPostRepository.findByStatusTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPost findBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPost findById(Long id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết ID: " + id));
    }

    @Override
    @Transactional
    public BlogPost savePost(BlogPost post) {
        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(SlugUtils.toSlug(post.getTitle()));
        }
        return blogPostRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        BlogPost post = findById(id);
        blogPostRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> findAll() {
        return blogPostRepository.findAll(Sort.by("createdAt").descending());
    }
}
