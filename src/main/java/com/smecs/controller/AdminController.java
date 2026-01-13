package com.smecs.controller;

import com.smecs.model.Category;
import com.smecs.model.Inventory;
import com.smecs.model.Product;
import com.smecs.service.CategoryService;
import com.smecs.service.InventoryService;
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
    private TableColumn<Product, Void> stockColumn;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextField productQuantityField;
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

    // Inventory management fields
    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, Integer> invIdColumn;
    @FXML
    private TableColumn<Inventory, String> invProductColumn;
    @FXML
    private TableColumn<Inventory, Integer> invQuantityColumn;
    @FXML
    private TableColumn<Inventory, String> invStatusColumn;
    @FXML
    private ComboBox<Product> inventoryProductComboBox;
    @FXML
    private TextField quantityField;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final ObservableList<Product> productList;
    private final ObservableList<Category> categoryList;
    private final ObservableList<Inventory> inventoryList;

    public AdminController() {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.inventoryService = new InventoryService();
        this.productList = FXCollections.observableArrayList();
        this.categoryList = FXCollections.observableArrayList();
        this.inventoryList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup product table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Setup stock column with badge cell factory
        if (stockColumn != null) {
            setupStockColumn();
        }

        productTable.setItems(productList);

        // Listen for selection changes
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));

        // Setup category table if available
        if (catIdColumn != null) {
            catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
            catNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            categoryTable.setItems(categoryList);

            // Listen for category selection changes
            categoryTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showCategoryDetails(newValue));
        }

        // Setup inventory table if available
        if (invIdColumn != null) {
            invIdColumn.setCellValueFactory(new PropertyValueFactory<>("inventoryId"));
            invProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            invQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invStatusColumn.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));

            inventoryTable.setItems(inventoryList);

            // Listen for inventory selection changes
            inventoryTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showInventoryDetails(newValue));
        }

        loadData();
    }

    private void setupStockColumn() {
        stockColumn.setCellFactory(column -> new TableCell<Product, Void>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    Inventory inventory = getInventoryForProduct(product.getProductId());

                    int quantity = inventory != null ? inventory.getQuantity() : 0;

                    badge.getStyleClass().removeAll("badge", "badge-success", "badge-warning", "badge-danger");
                    badge.getStyleClass().add("badge");

                    if (quantity <= 0) {
                        badge.setText("Out of Stock");
                        badge.getStyleClass().add("badge-danger");
                    } else if (quantity <= 10) {
                        badge.setText("Low Stock");
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.setText("In Stock");
                        badge.getStyleClass().add("badge-success");
                    }

                    setGraphic(badge);
                }
            }
        });
    }

    private Inventory getInventoryForProduct(int productId) {
        for (Inventory inv : inventoryList) {
            if (inv.getProductId() == productId) {
                return inv;
            }
        }
        return null;
    }

    private void loadData() {
        productList.setAll(productService.getAllProducts());
        categoryList.setAll(categoryService.getAllCategories());
        inventoryList.setAll(inventoryService.getAllInventory());
        categoryComboBox.setItems(categoryList);

        // Setup inventory product combo box with custom display
        if (inventoryProductComboBox != null) {
            inventoryProductComboBox.setItems(productList);
            inventoryProductComboBox.setButtonCell(new javafx.scene.control.ListCell<Product>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getProductName());
                }
            });
            inventoryProductComboBox.setCellFactory(lv -> new javafx.scene.control.ListCell<Product>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getProductName());
                }
            });
        }
    }

    private void showProductDetails(Product product) {
        if (product != null) {
            nameField.setText(product.getProductName());
            priceField.setText(product.getPrice() != null ? product.getPrice().toString() : "");
            descriptionArea.setText(product.getDescription());

            // Load inventory quantity for this product
            if (productQuantityField != null) {
                Inventory inventory = inventoryService.getInventoryByProductId(product.getProductId());
                productQuantityField.setText(inventory != null ? String.valueOf(inventory.getQuantity()) : "0");
            }

            // Select the category in combo box
            for (Category cat : categoryList) {
                if (cat.getCategoryId() == product.getCategoryId()) {
                    categoryComboBox.getSelectionModel().select(cat);
                    break;
                }
            }
        }
    }

    private void showCategoryDetails(Category category) {
        if (category != null && categoryNameField != null) {
            categoryNameField.setText(category.getCategoryName());
            categoryDescArea.setText(category.getDescription());
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

            int productId = productService.createProductAndGetId(product);

            // Create inventory record if quantity is provided
            if (productId > 0 && productQuantityField != null) {
                int quantity = getProductQuantityFromField();
                if (quantity >= 0) {
                    inventoryService.setInventoryForProduct(productId, quantity);
                }
            }

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

            // Update inventory quantity
            if (productQuantityField != null) {
                int quantity = getProductQuantityFromField();
                if (quantity >= 0) {
                    inventoryService.setInventoryForProduct(selected.getProductId(), quantity);
                }
            }

            loadData();
            clearForm();
        } else {
            showAlert("No Selection", "Please select a product to update.");
        }
    }

    private int getProductQuantityFromField() {
        if (productQuantityField == null || productQuantityField.getText() == null ||
            productQuantityField.getText().trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(productQuantityField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
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

    // Category management methods
    @FXML
    private void handleAddCategory() {
        if (isCategoryInputValid()) {
            Category category = new Category();
            category.setCategoryName(categoryNameField.getText().trim());
            category.setDescription(categoryDescArea.getText());

            if (categoryService.categoryNameExists(category.getCategoryName())) {
                showAlert("Duplicate Category", "A category with this name already exists.");
                return;
            }

            categoryService.createCategory(category);
            loadData();
            clearCategoryForm();
        }
    }

    @FXML
    private void handleUpdateCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null && isCategoryInputValid()) {
            selected.setCategoryName(categoryNameField.getText().trim());
            selected.setDescription(categoryDescArea.getText());

            categoryService.updateCategory(selected);
            loadData();
            clearCategoryForm();
        } else {
            showAlert("No Selection", "Please select a category to update.");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            categoryService.deleteCategory(selected.getCategoryId());
            loadData();
            clearCategoryForm();
        } else {
            showAlert("No Selection", "Please select a category to delete.");
        }
    }

    @FXML
    private void handleClearCategory() {
        clearCategoryForm();
        if (categoryTable != null) {
            categoryTable.getSelectionModel().clearSelection();
        }
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        descriptionArea.setText("");
        categoryComboBox.getSelectionModel().clearSelection();
        if (productQuantityField != null) {
            productQuantityField.setText("");
        }
    }

    private void clearCategoryForm() {
        if (categoryNameField != null) {
            categoryNameField.setText("");
        }
        if (categoryDescArea != null) {
            categoryDescArea.setText("");
        }
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

    private boolean isCategoryInputValid() {
        String errorMessage = "";

        if (categoryNameField == null || categoryNameField.getText() == null ||
            categoryNameField.getText().trim().isEmpty()) {
            errorMessage += "No valid category name!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Fields", errorMessage);
            return false;
        }
    }

    // Inventory management methods
    private void showInventoryDetails(Inventory inventory) {
        if (inventory != null && quantityField != null) {
            quantityField.setText(String.valueOf(inventory.getQuantity()));

            // Select the product in combo box
            for (Product product : productList) {
                if (product.getProductId() == inventory.getProductId()) {
                    inventoryProductComboBox.getSelectionModel().select(product);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleAddInventory() {
        if (isInventoryInputValid()) {
            Product selectedProduct = inventoryProductComboBox.getSelectionModel().getSelectedItem();

            // Check if inventory already exists for this product
            if (inventoryService.inventoryExistsForProduct(selectedProduct.getProductId())) {
                showAlert("Duplicate Entry", "Inventory already exists for this product. Please update instead.");
                return;
            }

            Inventory inventory = new Inventory();
            inventory.setProductId(selectedProduct.getProductId());
            inventory.setQuantity(Integer.parseInt(quantityField.getText().trim()));

            inventoryService.createInventory(inventory);
            loadData();
            clearInventoryForm();
        }
    }

    @FXML
    private void handleUpdateInventory() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null && isInventoryInputValid()) {
            selected.setQuantity(Integer.parseInt(quantityField.getText().trim()));

            inventoryService.updateInventory(selected);
            loadData();
            clearInventoryForm();
        } else if (selected == null) {
            showAlert("No Selection", "Please select an inventory item to update.");
        }
    }

    @FXML
    private void handleDeleteInventory() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            inventoryService.deleteInventory(selected.getInventoryId());
            loadData();
            clearInventoryForm();
        } else {
            showAlert("No Selection", "Please select an inventory item to delete.");
        }
    }

    @FXML
    private void handleClearInventory() {
        clearInventoryForm();
        if (inventoryTable != null) {
            inventoryTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleShowLowStock() {
        inventoryList.setAll(inventoryService.getLowStockItems(10));
    }

    @FXML
    private void handleShowAllInventory() {
        inventoryList.setAll(inventoryService.getAllInventory());
    }

    private void clearInventoryForm() {
        if (quantityField != null) {
            quantityField.setText("");
        }
        if (inventoryProductComboBox != null) {
            inventoryProductComboBox.getSelectionModel().clearSelection();
        }
    }

    private boolean isInventoryInputValid() {
        String errorMessage = "";

        if (inventoryProductComboBox == null ||
            inventoryProductComboBox.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Please select a product!\n";
        }
        if (quantityField == null || quantityField.getText() == null ||
            quantityField.getText().trim().isEmpty()) {
            errorMessage += "Please enter a quantity!\n";
        } else {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity < 0) {
                    errorMessage += "Quantity cannot be negative!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Quantity must be a whole number!\n";
            }
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
}
