package com.smecs.service.impl;

import com.smecs.dao.ReviewDAO;
import com.smecs.dao.UserDAO;
import com.smecs.dao.ProductDAO;
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

    private final ReviewDAO reviewDAO;
    private final UserDAO userDAO;
    private final ProductDAO productDAO;

    @Autowired
    public ReviewServiceImpl(ReviewDAO reviewDAO, UserDAO userDAO, ProductDAO productDAO) {
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
        this.productDAO = productDAO;
    }

    @Override
    public ReviewDTO createReview(CreateReviewRequestDTO request) {
        User user = userDAO.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        Product product = productDAO.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Review review = new Review();
        review.setUserId(user.getId());
        review.setProductId(product.getId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewDAO.save(review);
        return mapToDTO(review);
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request) {
        Review review = reviewDAO.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        review = reviewDAO.save(review);
        return mapToDTO(review);
    }

    @Override
    public PagedResponseDTO<ReviewDTO> getAllReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewDAO.findAll(pageable);
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
        if (!productDAO.existsById(productId)) {
           throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<Review> reviewPage = reviewDAO.findByProductId(productId, pageable);

        return getPagedResponse(reviewPage);
    }

    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewDAO.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        return mapToDTO(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        if (!reviewDAO.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        reviewDAO.deleteById(reviewId);
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
