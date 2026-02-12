package com.smecs.dao;

import com.smecs.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface InventoryDAO {
    Inventory save(Inventory inventory);
    Optional<Inventory> findById(Long id);
    Optional<Inventory> findByProductId(Long productId);
    Page<Inventory> searchInventory(String query, Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}

