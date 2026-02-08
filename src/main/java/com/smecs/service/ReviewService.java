package com.smecs.service;

import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ReviewDTO;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDTO createReview(CreateReviewRequestDTO request);
    PagedResponseDTO<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable);
    void deleteReview(Long reviewId);
}

