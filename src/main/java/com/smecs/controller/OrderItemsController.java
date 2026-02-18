package com.smecs.controller;

import com.smecs.annotation.RequireOwnership;
import com.smecs.annotation.RequireRole;
import com.smecs.dto.CreateOrderItemsRequestDTO;
import com.smecs.dto.OrderItemDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.OrderItem;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orderitems")
public class OrderItemsController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemsController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping("/{id}")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "orderItem", idParamName = "id")
    public ResponseEntity<ResponseDTO<OrderItemDTO>> getOrderItem(@PathVariable Long id) {
        return orderItemService.getOrderItemById(id)
                .map(item -> ResponseEntity.ok(new ResponseDTO<>("success", "Order item found", mapToDTO(item))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>("error", "Order item not found", null)));
    }

    @GetMapping("/order/{orderId}")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "order", idParamName = "orderId")
    public ResponseEntity<ResponseDTO<List<OrderItemDTO>>> getOrderItemsByOrder(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(orderId);
        List<OrderItemDTO> dtos = items.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Order items retrieved", dtos));
    }

    @PostMapping
    @RequireRole("customer")
    @RequireOwnership(resourceType = "order", idParamName = "orderId")
    public ResponseEntity<ResponseDTO<List<OrderItemDTO>>> createOrderItems(@RequestBody CreateOrderItemsRequestDTO request) {
        List<OrderItem> savedItems = orderItemService.createOrderItems(request.getOrderId(), request.getItems());
        List<OrderItemDTO> dtos = savedItems.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Order items created", dtos));
    }

    @PutMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<OrderItemDTO>> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDTO orderItemDTO) {
        try {
            OrderItem updatedItem = orderItemService.updateOrderItem(id, orderItemDTO);
            return ResponseEntity.ok(new ResponseDTO<>("success", "Order item updated", mapToDTO(updatedItem)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("error", e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteOrderItem(@PathVariable Long id) {
        try {
            orderItemService.deleteOrderItem(id);
            return ResponseEntity.ok(new ResponseDTO<>("success", "Order item deleted", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("error", e.getMessage(), null));
        }
    }

    private OrderItemDTO mapToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getOrderItemId());
        dto.setOrderId(item.getOrderId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPriceAtPurchase());
        return dto;
    }
}
