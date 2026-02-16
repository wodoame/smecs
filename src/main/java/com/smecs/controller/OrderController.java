package com.smecs.controller;

import com.smecs.annotation.RequireOwnership;
import com.smecs.annotation.RequireRole;
import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import com.smecs.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Order created", orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "order", idParamName = "id")
    public ResponseEntity<ResponseDTO<OrderDTO>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order found", orderService.getOrderById(id)));
    }

    @PutMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<OrderDTO>> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order status updated", orderService.updateOrderStatus(id, request)));
    }

    @GetMapping
    @RequireRole("admin")
    public ResponseEntity<PagedResponseDTO<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Convert 1-based page number to 0-based index
        int pageIndex = Math.max(0, page - 1);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortBy = sortParams.length > 0 ? sortParams[0] : "createdAt";
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/user/{userId}")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "user", idParamName = "userId")
    public ResponseEntity<PagedResponseDTO<OrderDTO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Convert 1-based page number to 0-based index
        int pageIndex = Math.max(0, page - 1);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortBy = sortParams.length > 0 ? sortParams[0] : "createdAt";
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "desc";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }

    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order deleted", null));
    }
}
