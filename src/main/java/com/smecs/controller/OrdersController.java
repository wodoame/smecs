package com.smecs.controller;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public ResponseDTO<OrderDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {
        return new ResponseDTO<>("success", "Order created", orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseDTO<OrderDTO> getOrder(@PathVariable Long id) {
        return new ResponseDTO<>("success", "Order found", orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseDTO<List<OrderDTO>> getAllOrders() {
        return new ResponseDTO<>("success", "Orders fetched", orderService.getAllOrders());
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return new ResponseDTO<>("success", "Order deleted", null);
    }
}
