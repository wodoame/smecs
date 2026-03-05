package com.smecs.controller;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.dto.UserLoginDTO;
import com.smecs.dto.UserResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.User;
import com.smecs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smecs.util.JwtUtil;
import com.smecs.service.CartService;
import com.smecs.entity.Cart;
import com.smecs.service.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CartService cartService;
    private final SecurityEventService securityEventService;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, CartService cartService,
                          SecurityEventService securityEventService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cartService = cartService;
        this.securityEventService = securityEventService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> register(@Valid @RequestBody UserRegisterDTO dto,
                                                                 HttpServletRequest request) {
        boolean success = userService.registerUser(dto);
        if (!success) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>("error", "Registration failed", null));
        }
        User user = userService.findByUsername(dto.getUsername());
        Cart cart = cartService.createCart(user.getId());
        String token = jwtUtil.generateToken(user, cart.getCartId());
        securityEventService.recordTokenIssued(user, token, request);
        UserResponseDTO response = mapToDTO(user);
        response.setToken(token);
        response.setCartId(cart.getCartId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> login(@Valid @RequestBody UserLoginDTO dto,
                                                              HttpServletRequest request) {
        User user = userService.authenticateUser(dto.getUsername(), dto.getPassword());
        if (user == null) {
            securityEventService.recordLoginFailure(dto.getUsername(), request);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO<>("error", "Invalid credentials", null));
        }
        Cart cart = cartService.createCart(user.getId());
        String token = jwtUtil.generateToken(user, cart.getCartId());
        securityEventService.recordLoginSuccess(user, request);
        securityEventService.recordTokenIssued(user, token, request);
        UserResponseDTO response = mapToDTO(user);
        response.setToken(token);
        response.setCartId(cart.getCartId());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Login successful", response));
    }

    @GetMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verify() {
        return ResponseEntity.noContent().build();
    }

    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}
