package com.smecs.service;

import com.smecs.entity.CartItem;
import java.util.List;

public interface CartItemService {
    CartItem saveCartItem(CartItem cartItem);
    List<CartItem> getCartItemsByCartId(Long cartId);
    void deleteCartItem(Long cartItemId);
}
