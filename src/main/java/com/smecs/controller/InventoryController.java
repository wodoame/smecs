package com.smecs.controller;

import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateInventoryRequestDTO;
import com.smecs.service.InventoryService;
import com.smecs.service.impl.InventoryCacheService;
import com.smecs.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryCacheService inventoryCacheService;

    @Autowired
    public InventoryController(InventoryService inventoryService, InventoryCacheService inventoryCacheService) {
        this.inventoryService = inventoryService;
        this.inventoryCacheService = inventoryCacheService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<InventoryDTO>> createInventory(@Valid @RequestBody CreateInventoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Inventory created", inventoryService.createInventory(request)));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PagedResponseDTO<InventoryDTO>>> searchInventory(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        String query = ""; // Empty query as filtering is removed
        Optional<PagedResponseDTO<InventoryDTO>> cached = inventoryCacheService.getSearchResults(query, page, size, sort);
        if (cached.isPresent()) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", cached.get()));
        }

        Sort sortSpec = PaginationUtils.parseSort(sort, "id");
        Pageable pageable = PageRequest.of(page - 1, size, sortSpec);

        PagedResponseDTO<InventoryDTO> result = inventoryService.searchInventory(pageable);
        inventoryCacheService.putSearchResults(query, page, size, sort, result);

        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", result));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseDTO<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory found", inventoryService.getInventoryByProductId(productId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<InventoryDTO>> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", inventoryService.getInventoryById(id)));
    }


    @PutMapping("/{inventoryId}")
    public ResponseEntity<ResponseDTO<InventoryDTO>> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateInventoryRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory updated",
                inventoryService.updateInventory(inventoryId, request)));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<ResponseDTO<Void>> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory with id " + inventoryId + " deleted successfuly", null));
    }
}
