package com.smecs.controller;

import com.smecs.model.Category;
import com.smecs.model.Product;
import com.smecs.service.CategoryService;
import com.smecs.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

public class AdminController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;

    // Category management fields
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Integer> catIdColumn;
    @FXML
    private TableColumn<Category, String> catNameColumn;
    @FXML
    private TableColumn<Category, String> catDescColumn;
    @FXML
    private TextField categoryNameField;
    @FXML
    private TextArea categoryDescArea;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ObservableList<Product> productList;
    private final ObservableList<Category> categoryList;

    public AdminController() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.productList = FXCollections.observableArrayList();
        this.categoryList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup product table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        productTable.setItems(productList);

        // Listen for selection changes
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));

        // Setup category table
        catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        catNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoryTable.setItems(categoryList);

        // Listen for category selection changes
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCategoryDetails(newValue));

        loadData();
    }

    private void loadData() {
        productList.setAll(productService.getAllProducts());
        categoryList.setAll(categoryService.getAllCategories());
        categoryComboBox.setItems(categoryList);
    }

    private void showProductDetails(Product product) {
        if (product != null) {
            nameField.setText(product.getProductName());
            priceField.setText(product.getPrice().toString());
            descriptionArea.setText(product.getDescription());

            // Select the category in ComboBox
            for (Category c : categoryList) {
                if (c.getCategoryId() == product.getCategoryId()) {
                    categoryComboBox.getSelectionModel().select(c);
                    break;
                }
            }
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleAdd() {
        if (isInputValid()) {
            Product product = new Product();
            product.setProductName(nameField.getText());
            product.setPrice(new BigDecimal(priceField.getText()));
            product.setDescription(descriptionArea.getText());
            product.setCategoryId(categoryComboBox.getSelectionModel().getSelectedItem().getCategoryId());
            product.setCreatedAt(Timestamp.from(Instant.now()));

            productService.createProduct(product);
            loadData();
            clearForm();
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null && isInputValid()) {
            selected.setProductName(nameField.getText());
            selected.setPrice(new BigDecimal(priceField.getText()));
            selected.setDescription(descriptionArea.getText());
            selected.setCategoryId(categoryComboBox.getSelectionModel().getSelectedItem().getCategoryId());

            productService.updateProduct(selected);
            loadData();
            clearForm();
        } else {
            showAlert("No Selection", "Please select a product to update.");
        }
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            productService.deleteProduct(selected.getProductId());
            loadData();
            clearForm();
        } else {
            showAlert("No Selection", "Please select a product to delete.");
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        productTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        descriptionArea.setText("");
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "No valid product name!\n";
        }
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage += "No valid price!\n";
        } else {
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Price must be a number!\n";
            }
        }
        if (categoryComboBox.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Please select a category!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Category Management Methods
    private void showCategoryDetails(Category category) {
        if (category != null) {
            categoryNameField.setText(category.getCategoryName());
            categoryDescArea.setText(category.getDescription());
        } else {
            clearCategoryForm();
        }
    }

    @FXML
    private void handleAddCategory() {
        if (isCategoryInputValid()) {
            // Check if category name already exists
            if (categoryService.categoryNameExists(categoryNameField.getText().trim())) {
                showAlert("Duplicate Category", "A category with this name already exists.");
                return;
            }

            Category category = new Category();
            category.setCategoryName(categoryNameField.getText().trim());
            category.setDescription(categoryDescArea.getText().trim());

            if (categoryService.createCategory(category)) {
                loadData();
                clearCategoryForm();
                showSuccessAlert("Success", "Category added successfully!");
            } else {
                showAlert("Error", "Failed to add category.");
            }
        }
    }

    @FXML
    private void handleUpdateCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null && isCategoryInputValid()) {
            selected.setCategoryName(categoryNameField.getText().trim());
            selected.setDescription(categoryDescArea.getText().trim());

            if (categoryService.updateCategory(selected)) {
                loadData();
                clearCategoryForm();
                showSuccessAlert("Success", "Category updated successfully!");
            } else {
                showAlert("Error", "Failed to update category.");
            }
        } else if (selected == null) {
            showAlert("No Selection", "Please select a category to update.");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Category");
            confirmAlert.setContentText("Are you sure you want to delete this category? Products using this category may be affected.");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (categoryService.deleteCategory(selected.getCategoryId())) {
                        loadData();
                        clearCategoryForm();
                        showSuccessAlert("Success", "Category deleted successfully!");
                    } else {
                        showAlert("Error", "Failed to delete category. It may be in use by products.");
                    }
                }
            });
        } else {
            showAlert("No Selection", "Please select a category to delete.");
        }
    }

    @FXML
    private void handleClearCategory() {
        clearCategoryForm();
        categoryTable.getSelectionModel().clearSelection();
    }

    private void clearCategoryForm() {
        categoryNameField.setText("");
        categoryDescArea.setText("");
    }

    private boolean isCategoryInputValid() {
        String errorMessage = "";

        if (categoryNameField.getText() == null || categoryNameField.getText().trim().isEmpty()) {
            errorMessage += "No valid category name!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
