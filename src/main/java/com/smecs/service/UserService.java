package com.smecs.service;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface UserService {
    boolean registerUser(UserRegisterDTO registrationDTO);

    User authenticateUser(String usernameOrEmail, String password);

    List<User> getAllUsers();

    User findByUsername(String username);

    User findById(Long id);

    User findByEmail(String email);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    boolean updateUser(User user);

    void deleteUser(Long userId);

    String hashPassword(String password);

    /**
     * Finds an existing user by provider + providerId, links an existing local
     * account by email if one exists, or creates a brand-new OAuth2 user.
     */
    User findOrCreateOAuthUser(OAuth2User oAuth2User, String provider);
}
