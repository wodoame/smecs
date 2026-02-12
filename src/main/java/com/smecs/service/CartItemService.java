package com.smecs.service;

import com.smecs.entity.CartItem;
import com.smecs.dto.AddToCartRequest;
import java.util.List;

public interface CartItemService {
    CartItem saveCartItem(CartItem cartItem);
    java.util.Optional<CartItem> getCartItemById(Long cartItemId);
    List<CartItem> getCartItemsByCartId(Long cartId);
    CartItem addItemToCart(AddToCartRequest request);
    CartItem updateCartItem(Long id, int quantity);
    void deleteCartItem(Long cartItemId);
    void deleteCartItemsByCartIdAndProductIds(Long cartId, List<Long> productIds);
}
