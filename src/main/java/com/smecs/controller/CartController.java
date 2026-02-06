package com.smecs.controller;

import com.smecs.dto.AddToCartRequest;
import com.smecs.dto.BatchAddToCartRequest;
import com.smecs.dto.CartItemDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateCartItemRequest;
import com.smecs.entity.CartItem;
import com.smecs.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO<CartItemDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        System.out.println(request);
        CartItem item = cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Item added to cart", mapToDTO(item)));
    }

    @PostMapping("/add-batch")
    public ResponseEntity<ResponseDTO<List<CartItemDTO>>> addItemsToCart(@Valid @RequestBody BatchAddToCartRequest request) {
        List<CartItem> items = cartService.addItemsToCart(request.getUserId(), request.getItems());
        List<CartItemDTO> dtos = items.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Items added to cart", dtos));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDTO<List<CartItemDTO>>> getCart(@PathVariable Long userId) {
        List<CartItem> items = cartService.getCartItems(userId);
        List<CartItemDTO> dtos = items.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Cart items retrieved successfully", dtos));
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDTO<CartItemDTO>> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request) {
        boolean updated = cartService.updateItemQuantity(request.getUserId(), request.getCartItemId(), request.getQuantity());
        if (updated) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "Cart item updated successfully", null));
        } else {
            return ResponseEntity.badRequest().body(new ResponseDTO<>("error", "Failed to update cart item", null));
        }
    }

    private CartItemDTO mapToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(item.getCartItemId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPriceAtAddition());
        dto.setSubtotal(item.getPriceAtAddition() * item.getQuantity());
        return dto;
    }
}
