package com.smecs.util;

import org.springframework.data.domain.Sort;

/**
 * Utility class for pagination and sorting operations.
 * Provides common helper methods used across controllers.
 */
public class PaginationUtils {

    private PaginationUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Parses a sort string into a Spring Data Sort object.
     *
     * @param sort String in format "property,direction" (e.g., "name,asc" or "price,desc")
     * @param defaultProperty The default property to sort by if sort is null or blank
     * @return Sort object configured with the specified or default sorting
     */
    public static Sort parseSort(String sort, String defaultProperty) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, defaultProperty);
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1
            ? Sort.Direction.fromString(parts[1].trim())
            : Sort.Direction.ASC;

        return Sort.by(direction, property);
    }

    /**
     * Parses a sort string into a Spring Data Sort object with default property "name".
     *
     * @param sort String in format "property,direction" (e.g., "name,asc" or "price,desc")
     * @return Sort object configured with the specified or default sorting
     */
    public static Sort parseSort(String sort) {
        return parseSort(sort, "id");
    }
}


