package com.smecs.controller;

import com.smecs.dao.UserDAO;
import com.smecs.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    private UserDAO userDAO;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public RegistrationController() {
        this.userDAO = new UserDAO();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", "error");
            return;
        }

        // Validate username length
        if (username.length() < 3) {
            showMessage("Username must be at least 3 characters long.", "error");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showMessage("Please enter a valid email address.", "error");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters long.", "error");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", "error");
            return;
        }

        try {
            // Check if username already exists
            if (userDAO.usernameExists(username)) {
                showMessage("Username already exists. Please choose another.", "error");
                return;
            }

            // Check if email already exists
            if (userDAO.emailExists(email)) {
                showMessage("Email already registered. Please use another email or login.", "error");
                return;
            }

            // Hash the password
            String hashedPassword = hashPassword(password);

            // Create new user object (default role is "customer")
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPasswordHash(hashedPassword);
            newUser.setRole("customer");

            // Save user to database
            if (userDAO.createUser(newUser)) {
                showMessage("Registration successful! Redirecting to login...", "success");

                // Wait a moment then redirect to login
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> handleBackToLogin(event));
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }).start();
            } else {
                showMessage("Registration failed. Please try again.", "error");
            }

        } catch (Exception e) {
            showMessage("Registration error: " + e.getMessage(), "error");
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();

            // Get stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("Login - Smart E-Commerce System");
            stage.show();

        } catch (IOException e) {
            showMessage("Error loading login page: " + e.getMessage(), "error");
            System.out.println(e.getMessage());
        }
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if (type.equals("error")) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (type.equals("success")) {
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

