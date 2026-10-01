package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.CheckoutForm;
import com.hvtc.bakery.entity.*;
import com.hvtc.bakery.repository.OrderRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").fullName("Test User").email("test@hvtc.vn").build();
        product = Product.builder().id(5L).name("Bánh Red Velvet").price(new BigDecimal("350000")).stockQuantity(10).status(true).build();
        
        CartItem cartItem = CartItem.builder().id(1L).product(product).quantity(1).build();
        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);
        
        cart = Cart.builder().id(100L).user(user).items(items).build();
        cartItem.setCart(cart);
    }

    @Test
    void testCreateOrderSuccessAndFreeShipping() {
        CheckoutForm form = CheckoutForm.builder()
                .customerName("Test User")
                .phone("0912345678")
                .email("test@hvtc.vn")
                .address("Số 12 Lê Văn Hiến")
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(cartService.getCartForUserOrSession(user, "session123")).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(user, "session123", form);

        assertNotNull(order);
        assertEquals(new BigDecimal("350000"), order.getTotalAmount());
        assertEquals(BigDecimal.ZERO, order.getShippingFee()); // Free shipping for >= 300k
        assertEquals(9, product.getStockQuantity()); // Stock deducted from 10 to 9
        verify(cartService, times(1)).clearCart(cart);
        verify(productRepository, times(1)).save(product);
    }
}
