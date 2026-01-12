package com.smecs.service;

import com.smecs.dao.UserDAO;
import com.smecs.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Service layer for User operations.
 * Provides business logic and validation for user management.
 */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new user with validation and password hashing
     */
    public boolean registerUser(String username, String email, String password, String role) {
        // Validate inputs
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        // Check for duplicates
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create user with hashed password
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(hashPassword(password));
        user.setRole(role != null ? role : "customer");

        return userDAO.createUser(user);
    }

    /**
     * Authenticate a user by username/email and password
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Try to find user by username or email
        User user = userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.trim());
        }

        // Validate credentials
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            return user;
        }

        return null; // Invalid credentials
    }

    /**
     * Get all users (admin function)
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user");
        }
        return userDAO.updateUser(user);
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return userDAO.deleteUser(userId);
    }

    /**
     * Hash password using SHA-256
     */
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}

