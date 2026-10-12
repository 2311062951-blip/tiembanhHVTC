package com.hvtc.bakery.repository;

import com.hvtc.bakery.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Optional<Product> findByProductCode(String productCode);

    List<Product> findByStatusTrue();
    List<Product> findByFeaturedTrueAndStatusTrue();
    List<Product> findByBestSellerTrueAndStatusTrue();

    List<Product> findByCategoryIdAndStatusTrue(Long categoryId);
    List<Product> findByCategorySlugAndStatusTrue(String categorySlug);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.status = true AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categorySlug IS NULL OR p.category.slug = :categorySlug) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> filterProducts(@Param("keyword") String keyword,
                                 @Param("categorySlug") String categorySlug,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    long countByStockQuantityLessThan(int threshold);
}
