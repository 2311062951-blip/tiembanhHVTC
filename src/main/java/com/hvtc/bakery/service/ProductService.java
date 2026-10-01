package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.ProductDto;
import com.hvtc.bakery.entity.Product;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> filterProducts(String keyword, String categorySlug, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int size);
    List<Product> findFeaturedProducts();
    List<Product> findBestSellers();
    List<Product> findByCategory(String categorySlug);
    Product findBySlug(String slug);
    Product findById(Long id);
    Product saveProduct(ProductDto dto);
    Product updateProduct(Long id, ProductDto dto);
    void deleteProduct(Long id);
    Product toggleFeatured(Long id);
    Product toggleBestSeller(Long id);
    List<Product> searchByName(String keyword);
    List<Product> findAll();
}
