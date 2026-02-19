package com.smecs.repository;

import com.smecs.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryRepositoryCustom {
    Page<Inventory> searchInventory(String query, Pageable pageable);
}

