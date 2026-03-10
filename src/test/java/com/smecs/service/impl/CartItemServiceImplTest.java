package com.smecs.service.impl;

import com.smecs.dto.AddToCartRequest;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Inventory;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CartItemRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.security.OwnershipChecks;
import com.smecs.service.CartService;
import com.smecs.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    @Test
    void addItemToCart_createsNewItemWhenNoneExists() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(9L);
        request.setQuantity(2);

        Cart cart = new Cart();
        cart.setCartId(7L);
        Product product = new Product();
        product.setId(9L);
        Inventory inventory = new Inventory();
        inventory.setQuantity(5);

        when(userService.requirePrincipal()).thenReturn(new SmecsUserPrincipal(7L, "alice", "alice@example.com", "CUSTOMER"));
        when(cartService.getOrCreateCartForUser(7L)).thenReturn(cart);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(7L, 9L)).thenReturn(null);
        when(inventoryRepository.findByProduct_Id(9L)).thenReturn(Optional.of(inventory));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartItemService.addItemToCart(request);

        assertThat(result.getCart()).isSameAs(cart);
        assertThat(result.getProduct()).isSameAs(product);
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void addItemToCart_throwsWhenInventoryInsufficient() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(9L);
        request.setQuantity(3);

        Cart cart = new Cart();
        cart.setCartId(7L);
        Product product = new Product();
        product.setId(9L);
        Inventory inventory = new Inventory();
        inventory.setQuantity(2);

        when(userService.requirePrincipal()).thenReturn(new SmecsUserPrincipal(7L, "alice", "alice@example.com", "CUSTOMER"));
        when(cartService.getOrCreateCartForUser(7L)).thenReturn(cart);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(7L, 9L)).thenReturn(null);
        when(inventoryRepository.findByProduct_Id(9L)).thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class, () -> cartItemService.addItemToCart(request));
    }

    @Test
    void updateCartItem_throwsWhenQuantityInvalid() {
        assertThrows(IllegalArgumentException.class, () -> cartItemService.updateCartItem(1L, 0));
    }

    @Test
    void deleteCartItem_enforcesOwnershipAndDeletes() {
        CartItem item = new CartItem();
        item.setCartItemId(4L);
        when(cartItemRepository.findById(4L)).thenReturn(Optional.of(item));

        cartItemService.deleteCartItem(4L);

        verify(ownershipChecks).assertCartItemOwnership(item);
        verify(cartItemRepository).delete(item);
    }

    @Test
    void getCartItemsByCartId_throwsWhenCartMissing() {
        when(cartService.getCartById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartItemService.getCartItemsByCartId(10L));
    }
}
