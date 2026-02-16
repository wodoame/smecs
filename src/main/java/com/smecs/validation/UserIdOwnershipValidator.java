package com.smecs.validation;

import org.springframework.stereotype.Component;

/**
 * Validates that the userId parameter matches the authenticated user's ID.
 * This is used for endpoints like GET /api/orders/user/{userId} where we want
 * to ensure a customer can only view their own orders.
 */
@Component
public class UserIdOwnershipValidator implements OwnershipValidator {

    @Override
    public boolean validateOwnership(Long requestedUserId, Long authenticatedUserId) {
        // Simply check if the requested userId matches the authenticated user's ID
        return requestedUserId.equals(authenticatedUserId);
    }

    @Override
    public String getResourceType() {
        return "user";
    }
}

