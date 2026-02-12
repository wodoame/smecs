package com.smecs.util;

import java.util.*;

/**
 * Manages role hierarchy and permissions.
 * Higher privilege roles automatically inherit permissions from lower privilege roles.
 */
public class RoleHierarchy {

    // Define role hierarchy levels (higher number = higher privilege)
    private static final Map<String, Integer> ROLE_LEVELS = new HashMap<>();

    static {
        ROLE_LEVELS.put("customer", 1);
        ROLE_LEVELS.put("admin", 2);
    }

    /**
     * Check if a user's role has sufficient privileges to access a resource
     * that requires any of the specified roles.
     *
     * @param userRole The role of the user
     * @param requiredRoles The roles required to access the resource
     * @return true if the user's role has sufficient privileges
     */
    public static boolean hasPermission(String userRole, String[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // No specific role required
        }

        if (userRole == null) {
            return false;
        }

        Integer userLevel = ROLE_LEVELS.getOrDefault(userRole.toLowerCase(), 0);

        // Check if user's role level is equal to or higher than any required role
        for (String requiredRole : requiredRoles) {
            Integer requiredLevel = ROLE_LEVELS.getOrDefault(requiredRole.toLowerCase(), Integer.MAX_VALUE);
            if (userLevel >= requiredLevel) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the privilege level of a role.
     *
     * @param role The role name
     * @return The privilege level (higher number = higher privilege)
     */
    public static int getRoleLevel(String role) {
        return ROLE_LEVELS.getOrDefault(role.toLowerCase(), 0);
    }

    /**
     * Check if one role has higher or equal privilege than another.
     *
     * @param role1 The first role
     * @param role2 The second role
     * @return true if role1 has higher or equal privilege than role2
     */
    public static boolean hasHigherOrEqualPrivilege(String role1, String role2) {
        return getRoleLevel(role1) >= getRoleLevel(role2);
    }
}

