package com.smecs.controller;

import com.smecs.model.Product;
import com.smecs.model.ReviewFeedback;
import com.smecs.model.User;
import com.smecs.service.FeedbackService;
import com.smecs.service.ProductService;
import com.smecs.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the review submission form.
 */
public class ReviewController {

    @FXML
    private Label headerLabel;
    @FXML
    private VBox productSelectionBox;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private ComboBox<Integer> ratingComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea reviewArea;
    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private Product product;
    private FeedbackService feedbackService;
    private ProductService productService;
    private boolean reviewSubmitted = false;

    @FXML
    public void initialize() {
        // Initialize rating options (1-5)
        ratingComboBox.setItems(FXCollections.observableArrayList(5, 4, 3, 2, 1));
        ratingComboBox.getSelectionModel().selectFirst(); // Default to 5 stars

        feedbackService = new FeedbackService();
        productService = new ProductService();

        // Setup product combo box
        productComboBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product object) {
                return object == null ? "" : object.getProductName();
            }

            @Override
            public Product fromString(String string) {
                return null; // Not needed
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            headerLabel.setText("Write a Review for: " + product.getProductName());
            productSelectionBox.setVisible(false);
            productSelectionBox.setManaged(false);
        } else {
            headerLabel.setText("Write a Review");
            productSelectionBox.setVisible(true);
            productSelectionBox.setManaged(true);
            loadProducts();
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productComboBox.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    public boolean isReviewSubmitted() {
        return reviewSubmitted;
    }

    @FXML
    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }

        // If product was selected from dropdown
        if (product == null) {
            product = productComboBox.getValue();
        }

        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showError("You must be logged in to submit a review.");
                return;
            }

            ReviewFeedback review = new ReviewFeedback();
            review.setProductId(product.getProductId());
            review.setProductName(product.getProductName());
            review.setUserId(currentUser.getUserId());
            review.setUserName(currentUser.getUsername());
            review.setUserEmail(currentUser.getEmail());

            review.setRating(ratingComboBox.getValue());
            review.setTitle(titleField.getText());
            review.setReviewText(reviewArea.getText());
            review.setVerifiedPurchase(false); // In a real app, check orders
            review.setCreatedAt(LocalDateTime.now());

            feedbackService.submitReview(review);

            reviewSubmitted = true;
            dialogStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error submitting review: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        if (product == null && productComboBox.getValue() == null) {
            showError("Please select a product.");
            return false;
        }

        if (ratingComboBox.getValue() == null) {
            showError("Please select a rating.");
            return false;
        }

        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showError("Please enter a title.");
            return false;
        }

        String text = reviewArea.getText();
        if (text == null || text.trim().isEmpty()) {
            showError("Please enter your review.");
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
