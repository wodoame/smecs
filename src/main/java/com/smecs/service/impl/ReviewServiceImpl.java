package com.smecs.service.impl;

import com.smecs.repository.ReviewRepository;
import com.smecs.repository.UserRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ReviewDTO;
import com.smecs.dto.UpdateReviewRequestDTO;
import com.smecs.entity.Product;
import com.smecs.entity.Review;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.ReviewService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ReviewDTO createReview(CreateReviewRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Review review = new Review();
        review.setUserId(user.getId());
        review.setProductId(product.getId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        return mapToDTO(review);
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        review = reviewRepository.save(review);
        return mapToDTO(review);
    }

    @Override
    public PagedResponseDTO<ReviewDTO> getAllReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);
        return getPagedResponse(reviewPage);
    }

    @NonNull
    private PagedResponseDTO<ReviewDTO> getPagedResponse(Page<Review> reviewPage) {
        List<ReviewDTO> content = reviewPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<ReviewDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(reviewPage));

        return pagedResponse;
    }

    @Override
    public PagedResponseDTO<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
           throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        return getPagedResponse(reviewPage);
    }

    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        return mapToDTO(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProductId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
