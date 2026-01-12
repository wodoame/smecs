package com.smecs.controller;

import com.smecs.cache.ProductCache;
import com.smecs.model.Product;
import com.smecs.service.ProductService;
import com.smecs.util.ProductSorter.SortCriteria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

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
    private TableColumn<Product, String> descriptionColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label cacheStatsLabel;

    private final ProductService productService;
    private final ObservableList<Product> productList;
    private SortCriteria currentSortCriteria = SortCriteria.NAME_ASC;

    public UserController() {
        this.productService = new ProductService();
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

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
        long endTime = System.currentTimeMillis();

        productList.setAll(products);
        updateStatus(String.format("Loaded %d products in %d ms", products.size(), endTime - startTime));
        updateCacheStats();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();

        long startTime = System.currentTimeMillis();
        List<Product> results = productService.searchProductsSorted(query, currentSortCriteria);
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
