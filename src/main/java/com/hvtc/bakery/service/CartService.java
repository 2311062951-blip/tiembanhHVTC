package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.User;

public interface CartService {
    Cart getCartForUserOrSession(User user, String sessionId);
    Cart addToCart(User user, String sessionId, Long productId, int quantity);
    Cart updateQuantity(User user, String sessionId, Long cartItemId, int quantity);
    Cart removeFromCart(User user, String sessionId, Long cartItemId);
    void clearCart(Cart cart);
    void mergeGuestCartToUser(String sessionId, User user);
}
