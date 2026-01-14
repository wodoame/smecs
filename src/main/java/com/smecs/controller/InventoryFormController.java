package com.smecs.controller;

import com.smecs.model.Inventory;
import com.smecs.model.Product;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class InventoryFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private TextField quantityField;

    private Stage dialogStage;
    private Inventory inventory;
    private InventoryService inventoryService;
    private ProductService productService;
    private boolean saveClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setServices(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;

        // Load products
        List<Product> products = productService.getAllProducts();
        productComboBox.setItems(FXCollections.observableArrayList(products));
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;

        if (inventory != null) {
            titleLabel.setText("Edit Inventory");
            quantityField.setText(String.valueOf(inventory.getQuantity()));
            productComboBox.setDisable(true); // Can't change product for existing inventory usually

            // Set product combo box
             for (Product p : productComboBox.getItems()) {
                 // Match by name as inventory likely has productName
                 if (p.getProductName().equals(inventory.getProductName())) {
                     productComboBox.setValue(p);
                     break;
                 }
             }
        } else {
            titleLabel.setText("Add Inventory");
            this.inventory = new Inventory();
            productComboBox.setDisable(false);
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            Product selectedProduct = productComboBox.getValue();
            int qty = Integer.parseInt(quantityField.getText());

            try {
                if (inventory.getInventoryId() != 0) {
                    // Update existing
                     inventory.setQuantity(qty);
                     // Ensure product ID is set if referenced
                     inventoryService.updateInventory(inventory);
                } else {
                    // New Inventory
                    inventory.setProductId(selectedProduct.getProductId());
                    inventory.setQuantity(qty);
                    // Set other defaults if needed, e.g. statumainaps
                    inventoryService.createInventory(inventory);
                }

                saveClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Error");
                alert.setHeaderText("Database Error");
                alert.setContentText("Could not save inventory: " + e.getMessage());
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

        if (productComboBox.getValue() == null) {
            errorMessage += "No product selected!\n";
        }
        if (quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
            errorMessage += "No quantity entered!\n";
        } else {
            try {
                int qty = Integer.parseInt(quantityField.getText());
                if (qty < 0) errorMessage += "Quantity must be non-negative!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Quantity must be an integer!\n";
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

