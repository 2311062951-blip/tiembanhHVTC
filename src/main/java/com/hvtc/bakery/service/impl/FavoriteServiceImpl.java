package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.entity.Favorite;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.repository.FavoriteRepository;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();

        favoriteRepository.save(favorite);
        return true;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) return false;
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
