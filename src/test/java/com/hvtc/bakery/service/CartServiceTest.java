package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.repository.CartItemRepository;
import com.hvtc.bakery.repository.CartRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        cart = Cart.builder().id(100L).user(user).items(new ArrayList<>()).build();
        product = Product.builder().id(5L).name("Bánh Croissant").price(new BigDecimal("45000")).stockQuantity(10).status(true).build();
    }

    @Test
    void testAddToCartSuccess() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 5L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        Cart updatedCart = cartService.addToCart(user, null, 5L, 2);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(2, updatedCart.getTotalQuantity());
        assertEquals(new BigDecimal("90000"), updatedCart.getTotalAmount());
    }

    @Test
    void testAddToCartExceedsStockThrowsException() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> cartService.addToCart(user, null, 5L, 99));
    }
}
