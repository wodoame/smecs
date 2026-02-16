package com.smecs.validation;

import com.smecs.dao.ReviewDAO;
import com.smecs.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public boolean validateOwnership(Long resourceId, Long userId) {
        return reviewDAO.findById(resourceId)
                .map(review -> review.getUserId().equals(userId))
                .orElse(false);
    }

    @Override
    public String getResourceType() {
        return "review";
    }
}

