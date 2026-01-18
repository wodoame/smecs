package com.smecs.controller;

import com.smecs.cache.ProductCache;
import com.smecs.model.CartItem;
import com.smecs.model.Inventory;
import com.smecs.model.Order;
import com.smecs.model.Product;
import com.smecs.model.User;
import com.smecs.service.CartService;
import com.smecs.service.InventoryService;
import com.smecs.service.OrderService;
import com.smecs.service.ProductService;
import com.smecs.util.ProductSorter.SortCriteria;
import com.smecs.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the user view with search, sort, and cache support.
 * Implements Epic 3 features: fast search and in-memory sorting.
 */
public class UserController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<SortCriteria> sortComboBox;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Void> stockColumn;
    @FXML
    private TableColumn<Product, String> descriptionColumn;
    @FXML
    private TableColumn<Product, Void> actionColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label cacheStatsLabel;
    @FXML
    private Label cartCountLabel;
    @FXML
    private Button viewCartButton;

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ObservableList<Product> productList;
    private Map<Integer, Inventory> inventoryMap;
    private SortCriteria currentSortCriteria = SortCriteria.NAME_ASC;

    public UserController() {
        this.productService = new ProductService();
        this.inventoryService = new InventoryService();
        this.cartService = new CartService();
        this.orderService = new OrderService();
        this.productList = FXCollections.observableArrayList();
        this.inventoryMap = new HashMap<>();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Setup stock column with badge cell factory
        setupStockColumn();

        // Setup action column with add to cart button
        setupActionColumn();

        productTable.setItems(productList);

        // Setup sort combo box
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(SortCriteria.values()));
            sortComboBox.setValue(SortCriteria.NAME_ASC);
            sortComboBox.setOnAction(e -> handleSortChange());
        }

        // Enable column sorting on click
        setupColumnSorting();

        loadProducts();
        updateCacheStats();
        updateCartCount();
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
                    Inventory inventory = inventoryMap.get(product.getProductId());

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

    private void setupActionColumn() {
        if (actionColumn == null) return;

        actionColumn.setCellFactory(column -> new TableCell<Product, Void>() {
            private final Button addToCartBtn = new Button();
            private final HBox container = new HBox();

            {
                // Setup cart icon using SVG path
                SVGPath cartIcon = new SVGPath();
                // Shopping cart icon path
                cartIcon.setContent("M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z");
                cartIcon.setFill(javafx.scene.paint.Color.WHITE);
                cartIcon.setScaleX(0.8);
                cartIcon.setScaleY(0.8);

                addToCartBtn.setGraphic(cartIcon);
                addToCartBtn.getStyleClass().addAll("button", "button-success", "button-icon-only");
                addToCartBtn.setTooltip(new Tooltip("Add to Cart"));

                container.setAlignment(Pos.CENTER);
                container.getChildren().add(addToCartBtn);

                addToCartBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleAddToCart(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    Inventory inventory = inventoryMap.get(product.getProductId());
                    int quantity = inventory != null ? inventory.getQuantity() : 0;

                    // Disable button if out of stock
                    addToCartBtn.setDisable(quantity <= 0);
                    setGraphic(container);
                }
            }
        });
    }

    private void handleAddToCart(Product product) {
        SessionManager session = SessionManager.getInstance();

        // Check if user is logged in
        if (!session.isLoggedIn()) {
            // Show login dialog
            boolean loggedIn = showLoginDialog();
            if (!loggedIn) {
                return; // User cancelled or login failed
            }
        }

        // User is now logged in, add to cart
        try {
            CartItem cartItem = cartService.addToCart(product, 1);
            if (cartItem != null) {
                updateCartCount();
                showAlert(Alert.AlertType.INFORMATION, "Added to Cart",
                    product.getProductName() + " has been added to your cart.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add item to cart. Please try again.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Error adding to cart: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewCart() {
        SessionManager session = SessionManager.getInstance();

        // Check if user is logged in
        if (!session.isLoggedIn()) {
            // Show login dialog
            boolean loggedIn = showLoginDialog();
            if (!loggedIn) {
                return; // User cancelled or login failed
            }
        }

        // Show the cart dialog
        showCartDialog();
    }

    private void showCartDialog() {
        try {
            // Get cart items
            java.util.List<CartItem> cartItems = cartService.getCartItems();

            if (cartItems.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Cart Empty",
                    "Your cart is empty. Add some products to get started!");
                return;
            }

            // Create a dialog to show cart contents
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Your Shopping Cart");
            dialog.setHeaderText("Cart Items (" + cartItems.size() + " items)");
            dialog.initModality(Modality.APPLICATION_MODAL);

            // Create a table for cart items
            TableView<CartItem> cartTable = new TableView<>();
            cartTable.setPrefWidth(700);
            cartTable.setPrefHeight(400);

            TableColumn<CartItem, String> productCol = new TableColumn<>("Product");
            productCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
            productCol.setPrefWidth(200);

            TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Quantity");
            qtyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
            qtyCol.setPrefWidth(80);

            TableColumn<CartItem, java.math.BigDecimal> priceCol = new TableColumn<>("Price");
            priceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("priceAtAddition"));
            priceCol.setPrefWidth(100);

            TableColumn<CartItem, java.math.BigDecimal> subtotalCol = new TableColumn<>("Subtotal");
            subtotalCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subtotal"));
            subtotalCol.setPrefWidth(100);

            // Action Column (Edit/Delete)
            TableColumn<CartItem, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setPrefWidth(200);
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button deleteBtn = new Button("Delete");
                private final Button updateBtn = new Button("Update");
                private final Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
                private final HBox pane = new HBox(5, qtySpinner, updateBtn, deleteBtn);

                {
                    deleteBtn.getStyleClass().add("button-danger");
                    deleteBtn.setOnAction(event -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        boolean removed = cartService.removeFromCart(item.getCartItemId());
                        if (removed) {
                            getTableView().getItems().remove(item);
                            updateCartCount(); // Update the main UI count
                            // Recalculate total
                            updateTotalLabel(getTableView().getItems(), (Label)dialog.getDialogPane().lookup("#totalLabel"));
                            if (getTableView().getItems().isEmpty()) {
                                dialog.close();
                            }
                        }
                    });

                    updateBtn.setOnAction(event -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        int newQty = qtySpinner.getValue();
                        boolean updated = cartService.updateItemQuantity(item.getCartItemId(), newQty);
                        if (updated) {
                            item.setQuantity(newQty);
                            getTableView().refresh();
                            updateTotalLabel(getTableView().getItems(), (Label)dialog.getDialogPane().lookup("#totalLabel"));
                        }
                    });

                    qtySpinner.setPrefWidth(70);
                    qtySpinner.setEditable(true);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        CartItem cartItem = getTableView().getItems().get(getIndex());
                        qtySpinner.getValueFactory().setValue(cartItem.getQuantity());
                        setGraphic(pane);
                    }
                }
            });

            cartTable.getColumns().addAll(productCol, qtyCol, priceCol, subtotalCol, actionCol);
            cartTable.setItems(FXCollections.observableArrayList(cartItems));

            Label totalLabel = new Label();
            totalLabel.setId("totalLabel");
            totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            updateTotalLabel(cartTable.getItems(), totalLabel);

            Button checkoutBtn = new Button("Place Order");
            checkoutBtn.getStyleClass().add("button-success");
            checkoutBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20;");
            checkoutBtn.setOnAction(e -> {
                try {
                    SessionManager session = SessionManager.getInstance();
                    Order order = orderService.placeOrder(session.getCurrentUserId());

                    dialog.close();
                    updateCartCount();

                    showAlert(Alert.AlertType.INFORMATION, "Order Placed",
                            "Your order #" + order.getOrderId() + " has been placed successfully!\n" +
                                    "Total Amount: $" + order.getTotalAmount());

                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Checkout Error", "Failed to place order: " + ex.getMessage());
                }
            });

            HBox footer = new HBox(20, totalLabel, checkoutBtn);
            footer.setAlignment(Pos.CENTER_RIGHT);

            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.getChildren().addAll(cartTable, footer);
            content.setPadding(new javafx.geometry.Insets(10));

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error loading cart: " + e.getMessage());
        }
    }

    private void updateTotalLabel(List<CartItem> items, Label label) {
        if (label == null) return;
        java.math.BigDecimal total = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        label.setText("Total: $" + total.toString());
    }

    private boolean showLoginDialog() {
        // Use an array to capture the result from the callback
        final boolean[] loginResult = {false};

        try {
            // Create a new Stage for the login dialog
            javafx.stage.Stage loginStage = new javafx.stage.Stage();
            loginStage.setTitle("Login Required - Smart E-Commerce System");
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.initOwner(productTable.getScene().getWindow());

            // Load the actual login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent loginContent = loader.load();

            // Get the LoginController and set dialog mode
            LoginController loginController = loader.getController();
            loginController.setDialogMode(loginStage, user -> {
                // Login was successful
                loginResult[0] = true;
            });

            // Create scene with the login view
            javafx.scene.Scene scene = new javafx.scene.Scene(loginContent);

            // Load the CSS stylesheet for consistent styling
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            loginStage.setScene(scene);
            loginStage.setResizable(false);

            // Show dialog and wait for it to close
            loginStage.showAndWait();

            // If login was successful, update the cart count
            if (loginResult[0]) {
                updateCartCount();
            }

            return loginResult[0];

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load login form: " + e.getMessage());
            return false;
        }
    }

    private void updateCartCount() {
        if (cartCountLabel != null) {
            int count = cartService.getCartItemCount();
            if (count > 0) {
                cartCountLabel.setText("Cart: " + count + " item" + (count > 1 ? "s" : ""));
                cartCountLabel.setVisible(true);
            } else {
                cartCountLabel.setText("Cart: Empty");
                cartCountLabel.setVisible(SessionManager.getInstance().isLoggedIn());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupColumnSorting() {
        // Allow clicking column headers to sort
        nameColumn.setSortable(true);
        categoryColumn.setSortable(true);
        priceColumn.setSortable(true);
        descriptionColumn.setSortable(true);

        productTable.setSortPolicy(table -> {
            TableColumn<Product, ?> sortColumn = table.getSortOrder().isEmpty() ? null : table.getSortOrder().get(0);
            if (sortColumn != null) {
                SortCriteria criteria = getSortCriteriaForColumn(sortColumn);
                if (criteria != null) {
                    currentSortCriteria = criteria;
                    if (sortComboBox != null) {
                        sortComboBox.setValue(criteria);
                    }
                }
            }
            return true;
        });
    }

    private SortCriteria getSortCriteriaForColumn(TableColumn<Product, ?> column) {
        boolean ascending = column.getSortType() == TableColumn.SortType.ASCENDING;

        if (column == nameColumn) {
            return ascending ? SortCriteria.NAME_ASC : SortCriteria.NAME_DESC;
        } else if (column == categoryColumn) {
            return ascending ? SortCriteria.CATEGORY_ASC : SortCriteria.CATEGORY_DESC;
        } else if (column == priceColumn) {
            return ascending ? SortCriteria.PRICE_ASC : SortCriteria.PRICE_DESC;
        }
        return null;
    }

    private void loadProducts() {
        long startTime = System.currentTimeMillis();
        List<Product> products = productService.getAllProductsSorted(currentSortCriteria);
        loadInventoryMap();
        long endTime = System.currentTimeMillis();

        productList.setAll(products);
        updateStatus(String.format("Loaded %d products in %d ms", products.size(), endTime - startTime));
        updateCacheStats();
    }

    private void loadInventoryMap() {
        inventoryMap.clear();
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        for (Inventory inv : inventoryList) {
            inventoryMap.put(inv.getProductId(), inv);
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();

        long startTime = System.currentTimeMillis();
        List<Product> results = productService.searchProductsSorted(query, currentSortCriteria);
        loadInventoryMap();
        long endTime = System.currentTimeMillis();

        productList.setAll(results);

        if (query == null || query.trim().isEmpty()) {
            updateStatus(String.format("Showing all %d products (%d ms)", results.size(), endTime - startTime));
        } else {
            updateStatus(String.format("Found %d products matching '%s' (%d ms)",
                    results.size(), query, endTime - startTime));
        }
        updateCacheStats();
    }

    @FXML
    private void handleSortChange() {
        if (sortComboBox != null && sortComboBox.getValue() != null) {
            currentSortCriteria = sortComboBox.getValue();

            // Re-apply sort to current results
            long startTime = System.currentTimeMillis();
            String query = searchField != null ? searchField.getText() : "";
            List<Product> results = productService.searchProductsSorted(query, currentSortCriteria);
            long endTime = System.currentTimeMillis();

            productList.setAll(results);
            updateStatus(String.format("Sorted by %s (%d ms)", currentSortCriteria.getDisplayName(), endTime - startTime));
            updateCacheStats();
        }
    }

    @FXML
    private void handleRefresh() {
        // Force cache invalidation and reload
        ProductCache.getInstance().invalidateAll();
        loadProducts();
        updateStatus("Data refreshed from database");
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void updateCacheStats() {
        if (cacheStatsLabel != null) {
            ProductCache.CacheStats stats = productService.getCacheStats();
            cacheStatsLabel.setText(String.format("Cache: %.1f%% hit rate (%d hits, %d misses)",
                    stats.getHitRate(), stats.hits, stats.misses));
        }
    }
}
