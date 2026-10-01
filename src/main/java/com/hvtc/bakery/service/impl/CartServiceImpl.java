package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.entity.Cart;
import com.hvtc.bakery.entity.CartItem;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.CartItemRepository;
import com.hvtc.bakery.repository.CartRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Cart getCartForUserOrSession(User user, String sessionId) {
        if (user != null) {
            return cartRepository.findByUser(user)
                    .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        } else if (sessionId != null && !sessionId.isBlank()) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> cartRepository.save(Cart.builder().sessionId(sessionId).build()));
        } else {
            throw new BadRequestException("Không thể xác định giỏ hàng (thiếu user và session)");
        }
    }

    @Override
    @Transactional
    public Cart addToCart(User user, String sessionId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Số lượng phải lớn hơn 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm bánh ID: " + productId));

        if (!product.isStatus()) {
            throw new BadRequestException("Sản phẩm bánh hiện đang ngừng bán");
        }

        Cart cart = getCartForUserOrSession(user, sessionId);

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Số lượng trong kho không đủ (chỉ còn " + product.getStockQuantity() + " bánh)");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            if (product.getStockQuantity() < quantity) {
                throw new BadRequestException("Số lượng trong kho không đủ (chỉ còn " + product.getStockQuantity() + " bánh)");
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateQuantity(User user, String sessionId, Long cartItemId, int quantity) {
        Cart cart = getCartForUserOrSession(user, sessionId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món bánh trong giỏ hàng"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Không có quyền chỉnh sửa mục giỏ hàng này");
        }

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            if (cartItem.getProduct().getStockQuantity() < quantity) {
                throw new BadRequestException("Kho không đủ bánh (chỉ còn " + cartItem.getProduct().getStockQuantity() + ")");
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeFromCart(User user, String sessionId, Long cartItemId) {
        Cart cart = getCartForUserOrSession(user, sessionId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món bánh trong giỏ hàng"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Không có quyền xóa mục giỏ hàng này");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Cart cart) {
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void mergeGuestCartToUser(String sessionId, User user) {
        if (sessionId == null || user == null) return;

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return;
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = getCartForUserOrSession(user, null);

        for (CartItem guestItem : guestCart.getItems()) {
            addToCart(user, null, guestItem.getProduct().getId(), guestItem.getQuantity());
        }

        cartRepository.delete(guestCart);
    }
}
