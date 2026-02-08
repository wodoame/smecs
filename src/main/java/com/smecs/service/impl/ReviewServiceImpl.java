package com.smecs.service.impl;

import com.smecs.dao.ReviewDAO;
import com.smecs.dao.UserDAO;
import com.smecs.dao.ProductDAO;
import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ReviewDTO;
import com.smecs.entity.Product;
import com.smecs.entity.Review;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.ReviewService;
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
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewDAO.save(review);
        return mapToDTO(review);
    }

    @Override
    public PagedResponseDTO<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable) {
        if (!productDAO.existsById(productId)) {
           throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<Review> reviewPage = reviewDAO.findByProductId(productId, pageable);

        List<ReviewDTO> content = reviewPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<ReviewDTO> pagedResponse = new PagedResponseDTO<>();
        PageMetadataDTO pageMetadata = new PageMetadataDTO();

        pageMetadata.setPage(reviewPage.getNumber());
        pageMetadata.setSize(reviewPage.getSize());
        pageMetadata.setTotalElements(reviewPage.getTotalElements());
        pageMetadata.setTotalPages(reviewPage.getTotalPages());
        pageMetadata.setFirst(reviewPage.isFirst());
        pageMetadata.setLast(reviewPage.isLast());
        pageMetadata.setEmpty(reviewPage.isEmpty());
        pageMetadata.setHasNext(reviewPage.hasNext());
        pageMetadata.setHasPrevious(reviewPage.hasPrevious());

        pagedResponse.setContent(content);
        pagedResponse.setPage(pageMetadata);

        return pagedResponse;
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
        dto.setUserId(review.getUser().getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserName(review.getUser().getUsername()); // Assuming User has username
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}

