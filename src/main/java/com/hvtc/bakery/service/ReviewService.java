package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.Review;
import java.util.List;

public interface ReviewService {
    List<Review> getProductReviews(Long productId);
    Review addReview(Long userId, Long productId, int rating, String comment);
    void deleteReview(Long reviewId);
    Review toggleReviewStatus(Long reviewId);
    Double getAverageRating(Long productId);
    Long getReviewCount(Long productId);
    List<Review> findAllReviews();
}
