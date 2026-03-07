package com.smecs.service.impl;

import com.smecs.entity.User;
import com.smecs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectProvider<AuthenticationManager> authenticationManagerProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticateUser_returnsUser_whenCredentialsValid() {
        when(authenticationManagerProvider.getObject()).thenReturn(authenticationManager);

        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = authenticationService.authenticateUser("alice", "secret");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(result).isSameAs(user);
    }

    @Test
    void authenticateUser_fallsBackToEmailLookup() {
        when(authenticationManagerProvider.getObject()).thenReturn(authenticationManager);
        when(userRepository.findByUsername("alice@example.com")).thenReturn(Optional.empty());

        User user = new User();
        user.setId(9L);
        user.setEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        User result = authenticationService.authenticateUser("alice@example.com", "secret");

        assertThat(result).isSameAs(user);
    }

    @Test
    void authenticateUser_returnsNull_whenAuthenticationFails() {
        when(authenticationManagerProvider.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        User result = authenticationService.authenticateUser("alice", "wrong");

        assertThat(result).isNull();
    }
}

