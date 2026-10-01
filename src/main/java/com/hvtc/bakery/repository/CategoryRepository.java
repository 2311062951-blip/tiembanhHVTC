package com.hvtc.bakery.repository;

import com.hvtc.bakery.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByStatusTrue();
    boolean existsBySlug(String slug);
}
