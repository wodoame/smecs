package com.smecs.controller;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.OrderQuery;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import com.smecs.entity.Order;
import com.smecs.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Order created", orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<OrderDTO>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order found", orderService.getOrderById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseDTO<OrderDTO>> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order status updated", orderService.updateOrderStatus(id, request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<PagedResponseDTO<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Order.Status status) {

        OrderQuery query = OrderQuery.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .status(status)
                .build();

        return ResponseEntity.ok(orderService.getAllOrders(query));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PagedResponseDTO<OrderDTO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        OrderQuery query = OrderQuery.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, query));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order deleted", null));
    }
}
