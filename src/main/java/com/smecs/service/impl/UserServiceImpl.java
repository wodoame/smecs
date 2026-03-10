package com.smecs.service.impl;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.exception.UnauthorizedException;
import com.smecs.repository.UserRepository;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "CUSTOMER", "STAFF");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(UserRegisterDTO registrationDTO) {
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
        user.setRole(resolveRole(role));
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return userRepository.save(user);
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
    public SmecsUserPrincipal requirePrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SmecsUserPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        if (principal.getUserId() == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal;
    }

    @Override
    public boolean isAdmin(SmecsUserPrincipal principal) {
        String role = principal.getRole();
        return role != null && role.equalsIgnoreCase("admin");
    }

    @Override
    @Transactional
    public User findOrCreateOAuthUser(OAuth2User oAuth2User, String provider) {
        String providerId = oAuth2User.getAttribute("sub");
        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");

        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        user = new User();
                        user.setEmail(email);
                        user.setUsername(deriveUsername(name, email));
                        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        user.setRole("CUSTOMER");
                    }
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    return userRepository.save(user);
                });
    }


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

    private String resolveRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "CUSTOMER";
        }
        String normalizedRole = role.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role. Allowed roles: ADMIN, CUSTOMER, STAFF");
        }
        return normalizedRole;
    }
}
