package com.smecs.repository;

import com.smecs.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>, InventoryRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByProduct_Id(Long productId);
}
