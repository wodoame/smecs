package com.smecs.service;

import com.smecs.entity.CartItem;
import com.smecs.dto.AddToCartRequest;
import java.util.List;
import java.util.Optional;
public interface CartItemService {
    Optional<CartItem> getCartItemById(Long cartItemId);
    List<CartItem> getCartItemsByCartId(Long cartId);
    CartItem addItemToCart(AddToCartRequest request);
    CartItem updateCartItem(Long id, int quantity);
    void deleteCartItem(Long cartItemId);
}
