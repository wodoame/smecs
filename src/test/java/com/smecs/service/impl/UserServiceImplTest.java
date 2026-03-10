package com.smecs.service.impl;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.User;
import com.smecs.exception.UnauthorizedException;
import com.smecs.repository.UserRepository;
import com.smecs.security.SmecsUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerUser_createsUserWithoutCart() {
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

        User result = userService.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    void registerUser_normalizesSupportedRoles() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("staffer");
        dto.setEmail("staffer@example.com");
        dto.setPassword("secret123");
        dto.setRole("staff");

        when(userRepository.existsByUsername("staffer")).thenReturn(false);
        when(userRepository.existsByEmail("staffer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        User result = userService.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getRole()).isEqualTo("STAFF");
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
