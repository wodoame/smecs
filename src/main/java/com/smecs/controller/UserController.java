package com.smecs.controller;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.dto.UserLoginDTO;
import com.smecs.dto.UserResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.entity.User;
import com.smecs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smecs.util.JwtUtil;
import com.smecs.annotation.RequireRole;

import java.util.List;
import java.util.stream.Collectors;

import com.smecs.service.CartService;
import com.smecs.entity.Cart;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CartService cartService;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, CartService cartService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cartService = cartService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> register(@Valid @RequestBody UserRegisterDTO dto) {
        boolean success = userService.registerUser(dto);
        if (!success) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>("error", "Registration failed", null));
        }
        User user = userService.findByUsername(dto.getUsername());
        String token = jwtUtil.generateToken(user);
        Cart cart = cartService.createCart(user.getId());
        UserResponseDTO response = mapToDTO(user);
        response.setToken(token);
        response.setCartId(cart.getCartId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> login(@Valid @RequestBody UserLoginDTO dto) {
        User user = userService.authenticateUser(dto.getUsername(), dto.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO<>("error", "Invalid credentials", null));
        }
        UserResponseDTO response = mapToDTO(user);
        String token = jwtUtil.generateToken(user);
        Cart cart = cartService.createCart(user.getId());
        response.setToken(token);
        response.setCartId(cart.getCartId());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Login successful", response));
    }

    @GetMapping("/users")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseDTO<>("success", "Users retrieved successfully", userDTOs));
    }

    @GetMapping("/users/{id}")
    @RequireRole({ "admin", "customer" })
    public ResponseEntity<ResponseDTO<UserResponseDTO>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>("error", "User not found", null));
        }
        return ResponseEntity.ok(new ResponseDTO<>("success", "User found", mapToDTO(user)));
    }

    @DeleteMapping("/users/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "User deleted successfully", null));
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
