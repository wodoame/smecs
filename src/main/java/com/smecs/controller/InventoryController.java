package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateInventoryRequestDTO;
import com.smecs.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<InventoryDTO>> createInventory(@Valid @RequestBody CreateInventoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Inventory created", inventoryService.createInventory(request)));
    }

    @GetMapping
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<PagedResponseDTO<InventoryDTO>>> searchInventory(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        PagedResponseDTO<InventoryDTO> result = inventoryService.searchInventory(query, page, size, sort);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", result));
    }

    @GetMapping("/product/{productId}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory found", inventoryService.getInventoryByProductId(productId)));
    }

    @GetMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<InventoryDTO>> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", inventoryService.getInventoryById(id)));
    }


    @PutMapping("/{inventoryId}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<InventoryDTO>> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateInventoryRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory updated",
                inventoryService.updateInventory(inventoryId, request)));
    }

    @DeleteMapping("/{inventoryId}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory with id " + inventoryId + " deleted successfuly", null));
    }
}
