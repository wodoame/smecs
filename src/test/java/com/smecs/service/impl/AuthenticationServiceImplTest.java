package com.smecs.service.impl;

import com.smecs.security.SmecsUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticateUser_returnsAuthentication_whenCredentialsValid() {
        SmecsUserPrincipal principal = new SmecsUserPrincipal(7L, "alice", "alice@example.com", "admin", "hashed");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        Authentication result = authenticationService.authenticateUser("alice", "secret");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isSameAs(principal);
    }

    @Test
    void authenticateUser_propagatesAuthenticationFailure() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        org.junit.jupiter.api.Assertions.assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticateUser("alice", "wrong"));
    }
}
