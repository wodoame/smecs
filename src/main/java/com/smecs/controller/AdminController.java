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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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
    @FXML
    private TableColumn<Product, Void> actionsColumn;

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
    @FXML
    private TableColumn<Category, Void> catActionsColumn;

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
    private TableColumn<Inventory, Void> invStatusColumn;
    @FXML
    private TableColumn<Inventory, Void> invActionsColumn;

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

        if (actionsColumn != null) {
            setupActionsColumn();
        }

        productTable.setItems(productList);

        // Setup category table if available
        if (catIdColumn != null) {
            catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
            catNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            categoryTable.setItems(categoryList);
        }

        if (catActionsColumn != null) {
            setupCategoryActionsColumn();
        }

        // Setup inventory table if available
        if (invIdColumn != null) {
            invIdColumn.setCellValueFactory(new PropertyValueFactory<>("inventoryId"));
            invProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            invQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            if (invStatusColumn != null) {
                setupInventoryStatusColumn();
            }
            if (invActionsColumn != null) {
                setupInventoryActionsColumn();
            }

            inventoryTable.setItems(inventoryList);
        }

        loadData();
    }

    private void setupStockColumn() {
        stockColumn.setCellFactory(column -> new TableCell<>() {
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

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                // Setup Edit Button
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.setFill(Color.WHITE);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("button-primary");
                editButton.setTooltip(new Tooltip("Update Product"));
                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    updateProduct(product);
                });

                // Setup Delete Button
                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.setFill(Color.WHITE);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setTooltip(new Tooltip("Delete Product"));
                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });

                // Initially invisible
                pane.setVisible(false);

                // Bind visibility to row hover
                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                     pane.visibleProperty().unbind();
                     if (newRow != null) {
                         pane.visibleProperty().bind(newRow.hoverProperty());
                     } else {
                         pane.setVisible(false);
                     }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupCategoryActionsColumn() {
        catActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                // Setup Edit Button
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.setFill(Color.WHITE);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("button-primary");
                editButton.setTooltip(new Tooltip("Update Category"));
                editButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    updateCategory(category);
                });

                // Setup Delete Button
                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.setFill(Color.WHITE);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setTooltip(new Tooltip("Delete Category"));
                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });

                // Initially invisible
                pane.setVisible(false);

                // Bind visibility to row hover
                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                     pane.visibleProperty().unbind();
                     if (newRow != null) {
                         pane.visibleProperty().bind(newRow.hoverProperty());
                     } else {
                         pane.setVisible(false);
                     }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupInventoryStatusColumn() {
        invStatusColumn.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Inventory inventory = getTableView().getItems().get(getIndex());
                    int quantity = inventory.getQuantity();

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

    private void setupInventoryActionsColumn() {
        invActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                // Setup Edit Button
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.setFill(Color.WHITE);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("button-primary");
                editButton.setTooltip(new Tooltip("Update Inventory Item"));
                editButton.setOnAction(event -> {
                    Inventory inventory = getTableView().getItems().get(getIndex());
                    updateInventory(inventory);
                });

                // Setup Delete Button
                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.setFill(Color.WHITE);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setTooltip(new Tooltip("Delete Inventory Item"));
                deleteButton.setOnAction(event -> {
                    Inventory inventory = getTableView().getItems().get(getIndex());
                    deleteInventory(inventory);
                });

                // Initially invisible
                pane.setVisible(false);

                // Bind visibility to row hover
                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                     pane.visibleProperty().unbind();
                     if (newRow != null) {
                         pane.visibleProperty().bind(newRow.hoverProperty());
                     } else {
                         pane.setVisible(false);
                     }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
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

    private void updateProduct(Product product) {
        boolean okClicked = showProductDialog(product);
        if (okClicked) {
            loadData();
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
            Scene scene = new Scene(page, 600, 500);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(600);

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

    private void deleteProduct(Product product) {
        if (showConfirmation("Delete Product", "Are you sure you want to delete " + product.getProductName() + "?")) {
           productService.deleteProduct(product.getProductId());
           loadData();
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

    private void updateCategory(Category category) {
        boolean okClicked = showCategoryDialog(category);
        if (okClicked) {
            loadData();
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
            Scene scene = new Scene(page, 450, 350);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(400);

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

    private void deleteCategory(Category category) {
         if (showConfirmation("Delete Category", "Are you sure you want to delete " + category.getCategoryName() + "?")) {
            categoryService.deleteCategory(category.getCategoryId());
            loadData();
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

    private void updateInventory(Inventory inventory) {
        boolean okClicked = showInventoryDialog(inventory);
        if (okClicked) {
            loadData();
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
            Scene scene = new Scene(page, 500, 300);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(300);

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

    private void deleteInventory(Inventory inventory) {
        if (showConfirmation("Delete Inventory", "Are you sure you want to delete this inventory item?")) {
            inventoryService.deleteInventory(inventory.getInventoryId());
            loadData();
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
