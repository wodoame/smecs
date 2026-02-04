package com.smecs.controller;

import com.smecs.dto.AddToCartRequest;
import com.smecs.dto.BatchAddToCartRequest;
import com.smecs.dto.CartItemDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.CartItem;
import com.smecs.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseDTO<CartItemDTO> addToCart(@RequestBody AddToCartRequest request) {
        CartItem item = cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
        return new ResponseDTO<>("success", "Item added to cart", mapToDTO(item));
    }

    @PostMapping("/add-batch")
    public ResponseDTO<List<CartItemDTO>> addItemsToCart(@RequestBody BatchAddToCartRequest request) {
        List<CartItem> items = cartService.addItemsToCart(request.getUserId(), request.getItems());
        List<CartItemDTO> dtos = items.stream().map(this::mapToDTO).collect(Collectors.toList());
        return new ResponseDTO<>("success", "Items added to cart", dtos);
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
