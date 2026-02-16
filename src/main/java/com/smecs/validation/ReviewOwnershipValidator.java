package com.smecs.validation;

import com.smecs.dao.ReviewDAO;
import com.smecs.entity.Review;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates ownership for Review resources.
 */
@Component
public class ReviewOwnershipValidator implements OwnershipValidator {

    private final ReviewDAO reviewDAO;

    @Autowired
    public ReviewOwnershipValidator(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        Optional<Review> reviewOpt = reviewDAO.findById(resourceId);

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

