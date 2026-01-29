package com.smecs.service.impl;

import com.smecs.model.User;
import com.smecs.dao.UserDAO;
import com.smecs.service.UserService;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public boolean registerUser(String username, String email, String password, String role) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(hashPassword(password));
        user.setRole(role != null ? role : "customer");
        return userDAO.createUser(user);
    }

    @Override
    public User authenticateUser(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        User user = userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.trim());
        }
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            return user;
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user");
        }
        return userDAO.updateUser(user);
    }

    @Override
    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return userDAO.deleteUser(userId);
    }

    @Override
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

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
