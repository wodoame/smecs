package com.smecs.service.impl;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.User;
import com.smecs.repository.UserRepository;
import com.smecs.service.UserService;
import com.smecs.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public boolean registerUser(UserRegisterDTO registrationDTO) {
        String username = registrationDTO.getUsername();
        String email = registrationDTO.getEmail();
        String password = registrationDTO.getPassword();
        String role = registrationDTO.getRole();

        if (!StringUtils.hasText(username) || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(hashPassword(password));
        user.setRole(role != null ? role : "customer");
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        return true;
    }

    @Override
    public User authenticateUser(String username, String password) {
        String principal = username.trim();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(principal, password));
            return findByUsernameOrEmail(principal);
        } catch (AuthenticationException ex) {
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public User findOrCreateOAuthUser(OAuth2User oAuth2User, String provider) {
        String providerId = oAuth2User.getAttribute("sub");
        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");

        // 1. Exact match by provider + providerId (returning user)
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 2. Same email already registered locally → link the account
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        // 3. Brand-new user — create one
                        user = new User();
                        user.setEmail(email);
                        user.setUsername(deriveUsername(name, email));
                        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        user.setRole("customer");
                    }
                     // Bind the OAuth2 identity
                     user.setProvider(provider);
                     user.setProviderId(providerId);
                     return userRepository.save(user);
                });
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /** Turns "Jane Doe" → "jane.doe", appending digits until the username is unique. */
    private String deriveUsername(String displayName, String email) {
        String base = (displayName != null && !displayName.isBlank())
                ? displayName.trim().toLowerCase().replaceAll("\\s+", ".")
                : email.split("@")[0];

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private User findByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail).orElse(null);
        if (user != null) {
            return user;
        }
        return userRepository.findByEmail(usernameOrEmail).orElse(null);
    }
}
