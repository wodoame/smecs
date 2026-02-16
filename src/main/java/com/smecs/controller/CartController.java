package com.smecs.controller;

import com.smecs.annotation.RequireOwnership;
import com.smecs.annotation.RequireRole;
import com.smecs.dto.CartDTO;
import com.smecs.dto.CreateCartRequest;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.CartItemService;
import com.smecs.entity.Cart;
import com.smecs.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @Autowired
    public CartController(CartService cartService, CartItemService cartItemService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    @PostMapping
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<CartDTO>> createCart(@Valid @RequestBody CreateCartRequest request) {
        Cart cart = cartService.createCart(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Cart created successfully", mapToCartDTO(cart)));
    }

    @DeleteMapping("/{cartId}/clear")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "cart", idParamName = "cartId")
    public ResponseEntity<ResponseDTO<Void>> clearCartItems(@PathVariable Long cartId) {
        int deleted = cartItemService.deleteAllItems(cartId);
        if (deleted == 0) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "No cart items to delete", null));
        }
        return ResponseEntity.ok(new ResponseDTO<>("success", deleted + " cart items deleted", null));
    }

    @GetMapping
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<List<CartDTO>>> getAllCarts() {
        List<Cart> carts = cartService.getAllCarts();
        List<CartDTO> dtos = carts.stream().map(this::mapToCartDTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Carts retrieved successfully", dtos));
    }

    @GetMapping("/{cartId}")
    @RequireRole("customer")
    @RequireOwnership(resourceType = "cart", idParamName = "cartId")
    public ResponseEntity<ResponseDTO<CartDTO>> getCartById(@PathVariable Long cartId) {
        return cartService.getCartById(cartId)
                .map(cart -> ResponseEntity
                        .ok(new ResponseDTO<>("success", "Cart retrieved successfully", mapToCartDTO(cart))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO<>("error", "Cart not found", null)));
    }

    @DeleteMapping("/{cartId}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Cart deleted successfully", null));
    }

    private CartDTO mapToCartDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
        }
        return dto;
    }
}
