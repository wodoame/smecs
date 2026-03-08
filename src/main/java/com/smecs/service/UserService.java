package com.smecs.service;

import com.smecs.dto.UserRegisterDTO;
import com.smecs.entity.User;
import com.smecs.security.SmecsUserPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface UserService {
    User registerUser(UserRegisterDTO registrationDTO);

    List<User> getAllUsers();

    User findByUsername(String username);

    User findById(Long id);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    void deleteUser(Long userId);

    String hashPassword(String password);

    SmecsUserPrincipal requirePrincipal();

    boolean isAdmin(SmecsUserPrincipal principal);

    /**
     * Finds an existing user by provider + providerId, links an existing local
     * account by email if one exists, or creates a brand-new OAuth2 user.
     */
    User findOrCreateOAuthUser(OAuth2User oAuth2User, String provider);
}
