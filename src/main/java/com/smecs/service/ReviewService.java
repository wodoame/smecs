package com.smecs.service;

import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ReviewDTO;
import com.smecs.dto.UpdateReviewRequestDTO;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDTO createReview(CreateReviewRequestDTO request);
    ReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request);
    PagedResponseDTO<ReviewDTO> getAllReviews(Pageable pageable);
    PagedResponseDTO<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable);
    ReviewDTO getReviewById(Long reviewId);
    void deleteReview(Long reviewId);
}
