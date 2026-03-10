package com.smecs.controller;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.dto.UserLoginDTO;
import com.smecs.dto.UserResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.User;
import com.smecs.exception.UnauthorizedException;
import com.smecs.security.AuthCookieUtils;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.AuthenticationService;
import com.smecs.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smecs.util.JwtUtil;
import com.smecs.service.SecurityEventService;
import com.smecs.service.TokenRevocationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.Arrays;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SecurityEventService securityEventService;
    private final TokenRevocationService tokenRevocationService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> register(@Valid @RequestBody UserRegisterDTO dto,
                                                                 HttpServletResponse response,
                                                                 HttpServletRequest request) {
        User user = userService.registerUser(dto);
        String token = jwtUtil.generateToken(user);
        AuthCookieUtils.addAccessTokenCookie(response, token, jwtUtil.getExpirationTimeSeconds());
        securityEventService.recordTokenIssued(user, token, request);
        UserResponseDTO responseBody = mapToDTO(user);
        responseBody.setToken(token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Registration successful", responseBody));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> login(@Valid @RequestBody UserLoginDTO dto,
                                                              HttpServletResponse response,
                                                              HttpServletRequest request) {
        try {
            Authentication authentication = authenticationService.authenticateUser(dto.getUsername(), dto.getPassword());
            SmecsUserPrincipal principal = requirePrincipal(authentication);
            String token = jwtUtil.generateToken(principal);
            AuthCookieUtils.addAccessTokenCookie(response, token, jwtUtil.getExpirationTimeSeconds());
            securityEventService.recordLoginSuccess(toUser(principal), request);
            securityEventService.recordTokenIssued(toUser(principal), token, request);
            UserResponseDTO responseBody = mapToDTO(principal);
            responseBody.setToken(token);
            return ResponseEntity.ok(new ResponseDTO<>("success", "Login successful", responseBody));
        } catch (AuthenticationException ex) {
            securityEventService.recordLoginFailure(dto.getUsername(), request);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO<>("error", "Invalid credentials", null));
        }
    }

    @GetMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verify() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> currentUser() {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        return ResponseEntity.ok(new ResponseDTO<>("success", "Authenticated user retrieved", mapToDTO(principal)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDTO<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtUtil.resolveToken(request);
        if (token == null || token.isBlank()) {
            AuthCookieUtils.clearAccessTokenCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO<>("error", "Missing authentication token", null));
        }
        Instant expiresAt = jwtUtil.extractExpiration(token).toInstant();
        tokenRevocationService.revokeToken(token, expiresAt);
        securityEventService.recordTokenRejected(token, request);
        AuthCookieUtils.clearAccessTokenCookie(response);

        return ResponseEntity.ok(new ResponseDTO<>("success", "Logout successful", null));
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    private UserResponseDTO mapToDTO(SmecsUserPrincipal principal) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(principal.getUserId());
        response.setUsername(principal.getUsername());
        response.setEmail(principal.getEmail());
        response.setRole(principal.getRole());
        return response;
    }

    private SmecsUserPrincipal requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SmecsUserPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    private User toUser(SmecsUserPrincipal principal) {
        User user = new User();
        user.setId(principal.getUserId());
        user.setUsername(principal.getUsername());
        user.setEmail(principal.getEmail());
        user.setRole(principal.getRole());
        return user;
    }
}
