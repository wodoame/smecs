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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.math.BigDecimal;
import java.util.Optional;

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

    // References to the main application for context if needed, but not strictly required if we use Stages.

    // Category management fields
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Integer> catIdColumn;
    @FXML
    private TableColumn<Category, String> catNameColumn;
    @FXML
    private TableColumn<Category, String> catDescColumn;

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

        // Setup category table if available
        if (catIdColumn != null) {
            catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
            catNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            categoryTable.setItems(categoryList);
        }

        // Setup inventory table if available
        if (invIdColumn != null) {
            invIdColumn.setCellValueFactory(new PropertyValueFactory<>("inventoryId"));
            invProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            invQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invStatusColumn.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));

            inventoryTable.setItems(inventoryList);
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
    }

    // --- Product Handlers ---

    @FXML
    private void handleAdd() {
        boolean okClicked = showProductDialog(null);
        if (okClicked) {
            loadData();
        }
    }

    @FXML
    private void handleUpdate() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            boolean okClicked = showProductDialog(selectedProduct);
            if (okClicked) {
                loadData();
            }
        } else {
            showAlert("No Selection", "No Product Selected", "Please select a product in the table.");
        }
    }

    private boolean showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/product_form.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Product Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ProductFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setServices(productService, categoryService, inventoryService);
            controller.setProduct(product);

            dialogStage.showAndWait();

            return controller.isSaveClicked();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleDelete() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            if (showConfirmation("Delete Product", "Are you sure you want to delete " + selectedProduct.getProductName() + "?")) {
               productService.deleteProduct(selectedProduct.getProductId());
               loadData();
            }
        } else {
            showAlert("No Selection", "No Product Selected", "Please select a product in the table.");
        }
    }

    // --- Category Handlers ---

    @FXML
    private void handleAddCategory() {
        boolean okClicked = showCategoryDialog(null);
        if (okClicked) {
            loadData();
        }
    }

    @FXML
    private void handleUpdateCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            boolean okClicked = showCategoryDialog(selectedCategory);
            if (okClicked) {
                loadData();
            }
        } else {
            showAlert("No Selection", "No Category Selected", "Please select a category in the table.");
        }
    }

    private boolean showCategoryDialog(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/category_form.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Category Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(categoryTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            CategoryFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCategoryService(categoryService);
            controller.setCategory(category);

            dialogStage.showAndWait();

            return controller.isSaveClicked();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            // Note: CategoryService usually returns boolean for delete
             if (showConfirmation("Delete Category", "Are you sure you want to delete " + selectedCategory.getCategoryName() + "?")) {
                categoryService.deleteCategory(selectedCategory.getCategoryId());
                loadData();
             }
        } else {
            showAlert("No Selection", "No Category Selected", "Please select a category in the table.");
        }
    }

    // --- Inventory Handlers ---

    @FXML
    private void handleAddInventory() {
        boolean okClicked = showInventoryDialog(null);
        if (okClicked) {
            loadData();
        }
    }

    @FXML
    private void handleUpdateInventory() {
        Inventory selectedInventory = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedInventory != null) {
            boolean okClicked = showInventoryDialog(selectedInventory);
            if (okClicked) {
                loadData();
            }
        } else {
            showAlert("No Selection", "No Inventory Item Selected", "Please select an item in the table.");
        }
    }

    private boolean showInventoryDialog(Inventory inventory) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/inventory_form.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Inventory Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(inventoryTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            InventoryFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setServices(inventoryService, productService);
            controller.setInventory(inventory);

            dialogStage.showAndWait();

            return controller.isSaveClicked();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleDeleteInventory() {
        Inventory selectedInventory = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedInventory != null) {
            if (showConfirmation("Delete Inventory", "Are you sure you want to delete this inventory item?")) {
                inventoryService.deleteInventory(selectedInventory.getInventoryId());
                loadData();
            }
        } else {
             showAlert("No Selection", "No Inventory Item Selected", "Please select an item in the table.");
        }
    }

    @FXML
    private void handleShowLowStock() {
        // Filter inventory to show only low stock items (quantity <= 10)
        inventoryList.setAll(
            inventoryService.getAllInventory().stream()
                .filter(inv -> inv.getQuantity() <= 10)
                .collect(java.util.stream.Collectors.toList())
        );
    }

    @FXML
    private void handleShowAllInventory() {
        // Reload all inventory items
        inventoryList.setAll(inventoryService.getAllInventory());
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
