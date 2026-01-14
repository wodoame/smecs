package com.smecs.controller;

import com.smecs.dao.UserDAO;
import com.smecs.model.User;
import com.smecs.util.SessionManager;
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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    private UserDAO userDAO;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", "error");
            return;
        }

        try {
            // Hash the password
            String hashedPassword = hashPassword(password);

            // Try to find user by username or email
            User user = userDAO.findByUsername(usernameOrEmail);
            if (user == null) {
                user = userDAO.findByEmail(usernameOrEmail);
            }

            // Validate credentials
            if (user != null && user.getPasswordHash().equals(hashedPassword)) {
                // Set the current user in session
                SessionManager.getInstance().setCurrentUser(user);

                showMessage("Login successful! Welcome " + user.getUsername(), "success");

                // Navigate to appropriate view based on role
                navigateToMainView(event, user);
            } else {
                showMessage("Invalid username/email or password.", "error");
            }

        } catch (Exception e) {
            showMessage("Login error: " + e.getMessage(), "error");
            System.out.println(e.getMessage());;
        }
    }

    @FXML
    private void handleRegisterLink(ActionEvent event) {
        try {
            // Load registration view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/registration_view.fxml"));
            Parent root = loader.load();

            // Get stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.setTitle("Registration - Smart E-Commerce System");
            stage.show();

        } catch (IOException e) {
            showMessage("Error loading registration page: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void navigateToMainView(ActionEvent event, User user) {
        try {
            String viewPath;
            String title;

            // Determine which view to load based on user role
            if ("admin".equalsIgnoreCase(user.getRole())) {
                viewPath = "/view/admin_view.fxml";
                title = "Admin Dashboard - Smart E-Commerce System";
            } else {
                viewPath = "/view/user_view.fxml";
                title = "User Dashboard - Smart E-Commerce System";
            }

            // Load the appropriate view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();

            // Pass user data to the controller if needed
            // Example: UserController controller = loader.getController();
            // controller.setUser(user);

            // Get stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle(title);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            showMessage("Error loading main view: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }
    }
}

