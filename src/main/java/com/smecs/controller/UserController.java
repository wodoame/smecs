package com.smecs.controller;

import com.smecs.model.Product;
import com.smecs.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController {

    @FXML
    private TextField searchField;
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

    private final ProductService productService;
    private final ObservableList<Product> productList;

    public UserController() {
        this.productService = new ProductService();
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        productTable.setItems(productList);
        loadProducts();
    }

    private void loadProducts() {
        productList.setAll(productService.getAllProducts());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        productList.setAll(productService.searchProducts(query));
    }
}
