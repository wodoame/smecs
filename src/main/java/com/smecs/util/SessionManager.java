//package com.smecs.util;
//
//import com.smecs.model.User;
//
///**
// * Singleton class to manage user session state across the application.
// * Tracks the currently logged-in user.
// */
//public class SessionManager {
//    private static SessionManager instance;
//    private User currentUser;
//
//    private SessionManager() {
//        // Private constructor for singleton pattern
//    }
//
//    /**
//     * Get the singleton instance of SessionManager.
//     */
//    public static synchronized SessionManager getInstance() {
//        if (instance == null) {
//            instance = new SessionManager();
//        }
//        return instance;
//    }
//
//    /**
//     * Set the currently logged-in user.
//     */
//    public void setCurrentUser(User user) {
//        this.currentUser = user;
//    }
//
//    /**
//     * Get the currently logged-in user.
//     */
//    public User getCurrentUser() {
//        return currentUser;
//    }
//
//    /**
//     * Check if a user is currently logged in.
//     */
//    public boolean isLoggedIn() {
//        return currentUser != null;
//    }
//
//    /**
//     * Get the current user's ID, or -1 if not logged in.
//     */
//    public int getCurrentUserId() {
//        return currentUser != null ? currentUser.getUserId() : -1;
//    }
//
//    /**
//     * Check if the current user is an admin.
//     */
//    public boolean isAdmin() {
//        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
//    }
//
//    /**
//     * Log out the current user.
//     */
//    public void logout() {
//        this.currentUser = null;
//    }
//
//    /**
//     * Get display name for the current user.
//     */
//    public String getDisplayName() {
//        return currentUser != null ? currentUser.getUsername() : "Guest";
//    }
//}

