package com.smecs.service.impl;

import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CartRepository;
import com.smecs.repository.UserRepository;
import com.smecs.security.OwnershipChecks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getCartById_enforcesOwnership() {
        Cart cart = new Cart();
        cart.setCartId(11L);
        when(cartRepository.findById(11L)).thenReturn(Optional.of(cart));

        Optional<Cart> result = cartService.getCartById(11L);

        assertThat(result).contains(cart);
        verify(ownershipChecks).assertCartOwnership(cart);
    }

    @Test
    void createCart_createsWithSharedUserId() {
        User user = new User();
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(cartRepository.findById(5L)).thenReturn(Optional.empty());

        Cart saved = new Cart();
        saved.setCartId(5L);
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        Cart result = cartService.createCart(5L);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        Cart created = captor.getValue();

        assertThat(created.getCartId()).isEqualTo(5L);
        assertThat(created.getUser()).isSameAs(user);
        assertThat(result.getCartId()).isEqualTo(5L);
    }

    @Test
    void createCart_returnsExistingCart() {
        User user = new User();
        user.setId(3L);
        Cart existing = new Cart();
        existing.setCartId(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(cartRepository.findById(3L)).thenReturn(Optional.of(existing));

        Cart result = cartService.createCart(3L);

        assertThat(result).isSameAs(existing);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void deleteCart_throwsWhenMissing() {
        when(cartRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteCart(99L));
    }
}

