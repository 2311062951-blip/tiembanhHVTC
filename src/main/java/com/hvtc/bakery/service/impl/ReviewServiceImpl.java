package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.entity.Review;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.ProductRepository;
import com.hvtc.bakery.repository.ReviewRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndStatusTrueOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional
    public Review addReview(Long userId, Long productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Đánh giá phải từ 1 đến 5 sao");
        }

        if (comment == null || comment.isBlank()) {
            throw new BadRequestException("Nội dung đánh giá không được để trống");
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment.trim())
                .status(true)
                .build();

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá ID: " + reviewId));
        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public Review toggleReviewStatus(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá ID: " + reviewId));
        review.setStatus(!review.isStatus());
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReviewCount(Long productId) {
        return reviewRepository.countByProductIdAndStatusTrue(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }
}
