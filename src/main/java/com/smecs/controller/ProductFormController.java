package com.smecs.controller;

import com.smecs.model.Category;
import com.smecs.model.Product;
import com.smecs.service.CategoryService;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class ProductFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private VBox quantityBox;
    @FXML
    private TextArea descriptionArea;

    private Stage dialogStage;
    private Product product;
    private ProductService productService;
    private CategoryService categoryService;
    private InventoryService inventoryService;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setServices(ProductService productService, CategoryService categoryService, InventoryService inventoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;

        // Load categories
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    public void setProduct(Product product) {
        this.product = product;

        if (product != null) {
            titleLabel.setText("Edit Product");
            nameField.setText(product.getProductName());
            priceField.setText(String.valueOf(product.getPrice()));
            descriptionArea.setText(product.getDescription());

            // Show quantity field when editing and load current inventory quantity
            if (quantityBox != null) {
                quantityBox.setVisible(true);
                quantityBox.setManaged(true);
            }
            if (quantityField != null && inventoryService != null) {
                // Load current inventory quantity
                var inventory = inventoryService.getInventoryByProductId(product.getProductId());
                int currentQty = (inventory != null) ? inventory.getQuantity() : 0;
                quantityField.setText(String.valueOf(currentQty));
            }

            // Set category
            if (product.getCategoryName() != null) {
                 for (Category c : categoryComboBox.getItems()) {
                     if (c.getCategoryName().equals(product.getCategoryName())) {
                         categoryComboBox.setValue(c);
                         break;
                     }
                 }
            }
        } else {
            titleLabel.setText("Add New Product");
            // Show quantity field for new products
            if (quantityBox != null) {
                quantityBox.setVisible(true);
                quantityBox.setManaged(true);
            }
            if (quantityField != null) {
                quantityField.setText("0");
            }
            // If new product, create a dummy one or handle in save
            this.product = new Product();
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            product.setProductName(nameField.getText());
            product.setPrice(new BigDecimal(priceField.getText()));
            product.setDescription(descriptionArea.getText());

            Category selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null) {
                // Assuming Product model has setCategoryId or similar to map the relationship.
                // However, the previous code used categoryName.
                // Let's check Product model later. For now assuming Service handles mapping if needed or we use what is available.
                product.setCategoryName(selectedCategory.getCategoryName());
                product.setCategoryId(selectedCategory.getCategoryId());
            }

            try {
                // Get the quantity from the field
                int quantity = 0;
                if (quantityField != null && inventoryService != null) {
                    try {
                        quantity = Integer.parseInt(quantityField.getText().trim());
                    } catch (NumberFormatException ignored) {
                        // Default to 0 if parsing fails
                    }
                }

                if (product.getProductId() != 0) {
                     // Update existing product
                     productService.updateProduct(product);

                     // Also update inventory quantity
                     if (quantityField != null && inventoryService != null) {
                         inventoryService.setInventoryForProduct(product.getProductId(), quantity);
                     }
                } else {
                    // Add new product and get the generated ID
                    int newProductId = productService.createProductAndGetId(product);
                    product.setProductId(newProductId);

                    // Also create inventory with initial quantity for new products
                    if (quantityField != null && inventoryService != null && newProductId > 0) {
                        inventoryService.setInventoryForProduct(newProductId, quantity);
                    }
                }
                saveClicked = true;
                dialogStage.close();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Error");
                alert.setHeaderText("Database Error");
                alert.setContentText("Could not save product: " + e.getMessage());
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

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "No valid product name!\n";
        }
        if (categoryComboBox.getValue() == null) {
            errorMessage += "No category selected!\n";
        }
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage += "No valid price!\n";
        } else {
            try {
                BigDecimal price = new BigDecimal(priceField.getText());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errorMessage += "Price must be non-negative!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "No valid price (must be a number)!\n";
            }
        }

        // Validate quantity field
        if (quantityField != null && quantityField.isVisible()) {
            String qtyText = quantityField.getText();
            if (qtyText != null && !qtyText.trim().isEmpty()) {
                try {
                    int qty = Integer.parseInt(qtyText.trim());
                    if (qty < 0) {
                        errorMessage += "Quantity must be non-negative!\n";
                    }
                } catch (NumberFormatException e) {
                    errorMessage += "Quantity must be a valid integer!\n";
                }
            }
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

