package com.smecs.validation;

import com.smecs.entity.Review;
import com.smecs.repository.ReviewRepository;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates ownership for Review resources.
 */
@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class ReviewOwnershipValidator implements OwnershipValidator {

    private final ReviewRepository reviewRepository;

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(resourceId);

        if (reviewOpt.isEmpty()) {
            throw new ResourceNotFoundException("Review not found with id: " + resourceId);
        }

        Review review = reviewOpt.get();
        if (!review.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this review");
        }
    }

    @Override
    public String getResourceType() {
        return "review";
    }
}
