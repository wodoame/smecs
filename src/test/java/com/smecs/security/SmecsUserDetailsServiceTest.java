package com.smecs.security;

import com.smecs.entity.User;
import com.smecs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmecsUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SmecsUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetails() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("hashed");
        user.setRole("admin");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getAuthorities()).anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("missing"));
    }

    @Test
    void loadUserByUsername_throwsWhenNoPasswordHash() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("alice"));
    }
}

