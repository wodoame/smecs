package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.AddToCartRequest;
import com.smecs.dto.CartItemDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.CartItem;
import com.smecs.entity.Product;
import com.smecs.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemsController {

    private final CartItemService cartItemService;

    @Autowired
    public CartItemsController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<CartItemDTO>> addCartItem(@RequestBody AddToCartRequest request) {
        CartItem createdItem = cartItemService.addItemToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Item added to cart", mapToDTO(createdItem)));
    }

    @GetMapping("/{id}")
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<CartItemDTO>> getCartItem(@PathVariable Long id) {
        return cartItemService.getCartItemById(id)
                .map(item -> ResponseEntity.ok(new ResponseDTO<>("success", "Cart item found", mapToDTO(item))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>("error", "Cart item not found", null)));
    }

    @GetMapping("/cart/{cartId}")
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<List<CartItemDTO>>> getCartItemsByCart(@PathVariable Long cartId) {
        List<CartItem> items = cartItemService.getCartItemsByCartId(cartId);
        List<CartItemDTO> dtos = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Cart items retrieved successfully", dtos));
    }

    @PutMapping("/{id}")
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<CartItemDTO>> updateCartItem(@PathVariable Long id,
            @RequestBody CartItemDTO dto) {
        CartItem updated = cartItemService.updateCartItem(id, dto.getQuantity());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Cart item updated", mapToDTO(updated)));
    }

    @DeleteMapping("/{id}")
    @RequireRole("customer")
    public ResponseEntity<ResponseDTO<Void>> deleteCartItem(@PathVariable Long id) {
        if (cartItemService.getCartItemById(id).isPresent()) {
            cartItemService.deleteCartItem(id);
            return ResponseEntity.ok(new ResponseDTO<>("success", "Cart item deleted", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("error", "Cart item not found", null));
        }
    }

    private CartItemDTO mapToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(item.getCartItemId());
        Product product = item.getProduct();
        if (product != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(product.getName());
            dto.setProductImage(product.getImageUrl());
            dto.setPrice(product.getPrice());
        }
        dto.setQuantity(item.getQuantity());
        double price = dto.getPrice();
        dto.setSubtotal(item.getQuantity() * price);
        return dto;
    }
}
