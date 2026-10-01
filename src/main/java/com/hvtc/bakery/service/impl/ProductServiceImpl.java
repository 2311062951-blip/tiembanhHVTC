package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.ProductDto;
import com.hvtc.bakery.entity.Category;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.CategoryRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.service.FileStorageService;
import com.hvtc.bakery.service.ProductService;
import com.hvtc.bakery.util.CodeGenerator;
import com.hvtc.bakery.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> filterProducts(String keyword, String categorySlug, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page, int size) {
        Sort sort = Sort.by("id").descending();

        if (sortBy != null) {
            switch (sortBy) {
                case "price-asc":
                    sort = Sort.by("price").ascending();
                    break;
                case "price-desc":
                    sort = Sort.by("price").descending();
                    break;
                case "newest":
                    sort = Sort.by("createdAt").descending();
                    break;
                case "best-seller":
                    sort = Sort.by("bestSeller").descending().and(Sort.by("id").descending());
                    break;
                default:
                    sort = Sort.by("id").descending();
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String catSlug = (categorySlug != null && !categorySlug.isBlank() && !"all".equalsIgnoreCase(categorySlug)) ? categorySlug.trim() : null;

        return productRepository.filterProducts(searchKeyword, catSlug, minPrice, maxPrice, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndStatusTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findBestSellers() {
        return productRepository.findByBestSellerTrueAndStatusTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(String categorySlug) {
        return productRepository.findByCategorySlugAndStatusTrue(categorySlug);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    @Transactional
    public Product saveProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục được chọn"));

        String slug = (dto.getSlug() != null && !dto.getSlug().isBlank())
                ? SlugUtils.toSlug(dto.getSlug())
                : SlugUtils.toSlug(dto.getName());

        if (productRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis() % 10000;
        }

        String code = (dto.getProductCode() != null && !dto.getProductCode().isBlank())
                ? dto.getProductCode().trim()
                : CodeGenerator.generateProductCode();

        String imagePath = dto.getImage();
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            imagePath = fileStorageService.storeFile(dto.getImageFile(), "products");
        }

        Product product = Product.builder()
                .productCode(code)
                .name(dto.getName().trim())
                .slug(slug)
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .price(dto.getPrice())
                .salePrice(dto.getSalePrice())
                .stockQuantity(dto.getStockQuantity())
                .image(imagePath)
                .category(category)
                .status(dto.isStatus())
                .featured(dto.isFeatured())
                .bestSeller(dto.isBestSeller())
                .build();

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto dto) {
        Product product = findById(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục được chọn"));

        product.setName(dto.getName().trim());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setShortDescription(dto.getShortDescription());
        product.setStatus(dto.isStatus());
        product.setFeatured(dto.isFeatured());
        product.setBestSeller(dto.isBestSeller());

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imagePath = fileStorageService.storeFile(dto.getImageFile(), "products");
            product.setImage(imagePath);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public Product toggleFeatured(Long id) {
        Product product = findById(id);
        product.setFeatured(!product.isFeatured());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product toggleBestSeller(Long id) {
        Product product = findById(id);
        product.setBestSeller(!product.isBestSeller());
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return productRepository.searchByName(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
