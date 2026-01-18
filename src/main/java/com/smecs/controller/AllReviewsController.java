package com.smecs.controller;

import com.smecs.model.Product;
import com.smecs.model.ReviewFeedback;
import com.smecs.service.FeedbackService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AllReviewsController {

    @FXML
    private TableView<ReviewFeedback> reviewsTable;
    @FXML
    private TableColumn<ReviewFeedback, String> productColumn;
    @FXML
    private TableColumn<ReviewFeedback, String> userColumn;
    @FXML
    private TableColumn<ReviewFeedback, Integer> ratingColumn;
    @FXML
    private TableColumn<ReviewFeedback, String> titleColumn;
    @FXML
    private TableColumn<ReviewFeedback, String> reviewColumn;
    @FXML
    private TableColumn<ReviewFeedback, String> dateColumn;
    @FXML
    private Label headerLabel;

    private FeedbackService feedbackService;
    private Product currentProduct;

    @FXML
    public void initialize() {
        feedbackService = new FeedbackService();

        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("reviewText"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadReviews();
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        if (product != null) {
            headerLabel.setText("Reviews for: " + product.getProductName());
            productColumn.setVisible(false); // Hide product column as it's redundant
        } else {
            headerLabel.setText("All Reviews");
            productColumn.setVisible(true);
        }
        loadReviews();
    }

    @FXML
    private void handleRefresh() {
        loadReviews();
    }

    private void loadReviews() {
        try {
            if (currentProduct != null) {
                reviewsTable.setItems(FXCollections.observableArrayList(
                    feedbackService.getProductReviews(currentProduct.getProductId())
                ));
            } else {
                reviewsTable.setItems(FXCollections.observableArrayList(feedbackService.getAllReviews()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddReview() {
        // ReviewController checks login status internally when submitting,
        // but checking here might be better UX.
        // For simplicity relying on ReviewController's check.

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/review_form.fxml"));
            Parent root = loader.load();

            ReviewController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Write Review");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

             Scene scene = new Scene(root);
             try {
                java.net.URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
             } catch(Exception e) {}

            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            controller.setProduct(currentProduct); // Pass current product if set

            dialogStage.showAndWait();

            if (controller.isReviewSubmitted()) {
                handleRefresh();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
