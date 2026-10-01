package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.Favorite;
import java.util.List;

public interface FavoriteService {
    List<Favorite> getUserFavorites(Long userId);
    boolean addFavorite(Long userId, Long productId);
    boolean removeFavorite(Long userId, Long productId);
    boolean isFavorite(Long userId, Long productId);
}
