package com.smecs.validation;

import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;

/**
 * Interface for validating resource ownership.
 * Each resource type (Review, Order, etc.) should have its own implementation.
 */
public interface OwnershipValidator {
    /**
     * Validates that the specified user owns the specified resource.
     *
     * @param resourceId The ID of the resource to check
     * @param userId The ID of the user claiming ownership
     * @throws ResourceNotFoundException if the resource does not exist (404)
     * @throws ForbiddenException if the resource exists but user doesn't own it (403)
     */
    void validateOwnership(Long resourceId, Long userId);

    /**
     * Returns the resource type this validator handles (e.g., "review", "order")
     */
    String getResourceType();
}

