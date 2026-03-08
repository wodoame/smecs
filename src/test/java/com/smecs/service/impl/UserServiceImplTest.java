package com.smecs.service.impl;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.exception.UnauthorizedException;
import com.smecs.repository.CartRepository;
import com.smecs.repository.UserRepository;
import com.smecs.security.SmecsUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUser_createsUserAndCart() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("secret123");
        dto.setRole("customer");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            if (cart.getUser() != null) {
                cart.setCartId(cart.getUser().getId());
            }
            return cart;
        });

        User result = userService.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart cart = cartCaptor.getValue();
        assertThat(cart.getCartId()).isEqualTo(42L);
        assertThat(cart.getUser().getId()).isEqualTo(42L);
    }

    @Test
    void registerUser_whenUserDoesNotExist_createsUserAndAssignsCartWithSameId() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("newuser@example.com");
        dto.setPassword("secret123");
        dto.setRole("customer");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            if (cart.getUser() != null) {
                cart.setCartId(cart.getUser().getId());
            }
            return cart;
        });

        User result = userService.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertThat(createdUser.getUsername()).isEqualTo("newuser");
        assertThat(createdUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(createdUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(createdUser.getId()).isEqualTo(101L);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(1)).save(cartCaptor.capture());
        Cart createdCart = cartCaptor.getValue();
        assertThat(createdCart.getUser()).isSameAs(createdUser);
        assertThat(createdCart.getCartId()).isEqualTo(createdUser.getId());
    }

    @Test
    void requirePrincipal_throwsWhenMissing() {
        assertThrows(UnauthorizedException.class, () -> userService.requirePrincipal());
    }

    @Test
    void requirePrincipal_returnsPrincipalWhenPresent() {
        SmecsUserPrincipal principal = new SmecsUserPrincipal(9L, "alice", "alice@example.com", "customer");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        SmecsUserPrincipal result = userService.requirePrincipal();

        assertThat(result.getUserId()).isEqualTo(9L);
    }

    @Test
    void deleteUser_throwsForInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(0L));
    }

    @Test
    void findByUsername_returnsNullWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThat(userService.findByUsername("missing")).isNull();
    }
}
