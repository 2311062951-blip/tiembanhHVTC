package com.hvtc.bakery.repository;

import com.hvtc.bakery.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);
    List<BlogPost> findTop6ByStatusTrueOrderByCreatedAtDesc();
    Page<BlogPost> findByStatusTrue(Pageable pageable);
    Page<BlogPost> findByStatusTrueAndTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
