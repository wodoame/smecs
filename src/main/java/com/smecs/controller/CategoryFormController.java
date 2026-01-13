package com.smecs.controller;

import com.smecs.model.Category;
import com.smecs.service.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CategoryFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField categoryNameField;
    @FXML
    private TextArea categoryDescArea;

    private Stage dialogStage;
    private Category category;
    private CategoryService categoryService;
    private boolean saveClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setCategory(Category category) {
        this.category = category;

        if (category != null) {
            titleLabel.setText("Edit Category");
            categoryNameField.setText(category.getCategoryName());
            categoryDescArea.setText(category.getDescription());
        } else {
            titleLabel.setText("Add New Category");
            this.category = new Category();
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            category.setCategoryName(categoryNameField.getText());
            category.setDescription(categoryDescArea.getText());

            boolean success;
            if (category.getCategoryId() != 0) {
                // Update
                success = categoryService.updateCategory(category);
            } else {
                // Add
                success = categoryService.createCategory(category);
            }

            if (success) {
                saveClicked = true;
                dialogStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Error");
                alert.setHeaderText("Database Error");
                alert.setContentText("Could not save category.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (categoryNameField.getText() == null || categoryNameField.getText().trim().isEmpty()) {
            errorMessage += "No valid category name!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}

