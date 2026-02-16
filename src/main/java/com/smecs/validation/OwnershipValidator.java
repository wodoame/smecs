package com.smecs.validation;

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
     * @return true if the user owns the resource, false otherwise
     */
    boolean validateOwnership(Long resourceId, Long userId);

    /**
     * Returns the resource type this validator handles (e.g., "review", "order")
     */
    String getResourceType();
}

