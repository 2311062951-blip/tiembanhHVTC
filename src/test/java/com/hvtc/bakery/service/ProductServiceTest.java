package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.ProductDto;
import com.hvtc.bakery.entity.Category;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.repository.CategoryRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Bánh ngọt")
                .slug("banh-ngot")
                .status(true)
                .build();

        product = Product.builder()
                .id(10L)
                .productCode("BAKE-100")
                .name("Bánh Mousse Matcha")
                .slug("banh-mousse-matcha")
                .price(new BigDecimal("280000"))
                .salePrice(new BigDecimal("250000"))
                .stockQuantity(20)
                .category(category)
                .status(true)
                .build();
    }

    @Test
    void testFindByIdSuccess() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        Product found = productService.findById(10L);

        assertNotNull(found);
        assertEquals("Bánh Mousse Matcha", found.getName());
        assertEquals(new BigDecimal("250000"), found.getEffectivePrice());
        verify(productRepository, times(1)).findById(10L);
    }

    @Test
    void testSaveProductSuccess() {
        ProductDto dto = ProductDto.builder()
                .name("Bánh Tiramisu Ý")
                .categoryId(1L)
                .price(new BigDecimal("320000"))
                .stockQuantity(15)
                .status(true)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product saved = productService.saveProduct(dto);

        assertNotNull(saved);
        assertEquals("Bánh Tiramisu Ý", saved.getName());
        assertEquals(new BigDecimal("320000"), saved.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
