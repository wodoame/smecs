package com.smecs.service;

import com.smecs.dao.InventoryDAO;
import com.smecs.model.Inventory;

import java.util.List;

public class InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public void createInventory(Inventory inventory) {
        inventoryDAO.addInventory(inventory);
    }

    public void updateInventory(Inventory inventory) {
        inventoryDAO.updateInventory(inventory);
    }

    public void updateQuantityByProductId(int productId, int quantity) {
        inventoryDAO.updateQuantityByProductId(productId, quantity);
    }

    public void deleteInventory(int inventoryId) {
        inventoryDAO.deleteInventory(inventoryId);
    }

    public List<Inventory> getAllInventory() {
        return inventoryDAO.findAll();
    }

    public Inventory getInventoryByProductId(int productId) {
        return inventoryDAO.findByProductId(productId);
    }

    public boolean inventoryExistsForProduct(int productId) {
        return inventoryDAO.inventoryExistsForProduct(productId);
    }

    /**
     * Creates or updates inventory for a product.
     * If inventory already exists for the product, it updates the quantity.
     * Otherwise, it creates a new inventory record.
     */
    public void setInventoryForProduct(int productId, int quantity) {
        if (inventoryExistsForProduct(productId)) {
            updateQuantityByProductId(productId, quantity);
        } else {
            Inventory inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setQuantity(quantity);
            createInventory(inventory);
        }
    }
}

