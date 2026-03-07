package com.smecs.service.impl;

import com.smecs.dto.CreateReviewRequestDTO;
import com.smecs.dto.ReviewDTO;
import com.smecs.dto.UpdateReviewRequestDTO;
import com.smecs.entity.Product;
import com.smecs.entity.Review;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.ReviewRepository;
import com.smecs.repository.UserRepository;
import com.smecs.security.OwnershipChecks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void createReview_persistsReview() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO();
        request.setUserId(7L);
        request.setProductId(9L);
        request.setRating(5);
        request.setComment("Nice");

        User user = new User();
        user.setId(7L);
        Product product = new Product();
        product.setId(9L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(3L);
            return review;
        });

        ReviewDTO result = reviewService.createReview(request);

        verify(ownershipChecks).assertUserMatches(7L);
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getProductId()).isEqualTo(9L);
        assertThat(result.getRating()).isEqualTo(5);
    }

    @Test
    void updateReview_updatesFields() {
        Review review = new Review();
        review.setId(4L);
        review.setRating(3);
        review.setComment("old");
        when(reviewRepository.findById(4L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateReviewRequestDTO request = new UpdateReviewRequestDTO();
        request.setRating(4);
        request.setComment("new");

        ReviewDTO result = reviewService.updateReview(4L, request);

        verify(ownershipChecks).assertReviewOwnership(review);
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("new");
    }

    @Test
    void deleteReview_throwsWhenMissing() {
        when(reviewRepository.findById(33L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(33L));
    }
}

