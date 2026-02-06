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

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserResponseDTO>> register(@Valid @RequestBody UserRegisterDTO dto) {
        boolean success = userService.registerUser(dto.getUsername(), dto.getEmail(), dto.getPassword(), null);
        if (!success) {
            return ResponseEntity.badRequest().body(new ResponseDTO<>("error", "Registration failed", null));
        }
        User user = userService.findByUsername(dto.getUsername());
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole()); // fallback to Lombok getter
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
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        // Set authentication expiry to 5 minutes from now
        response.setAuthExpiry(System.currentTimeMillis() + 5 * 60_000L);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Login successful", response));
    }
}
