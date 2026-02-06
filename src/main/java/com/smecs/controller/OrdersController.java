package com.smecs.controller;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderService orderService;

    @Autowired
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Order created", orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<OrderDTO>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order found", orderService.getOrderById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<OrderDTO>>> getAllOrders() {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Orders fetched", orderService.getAllOrders()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order deleted", null));
    }
}
