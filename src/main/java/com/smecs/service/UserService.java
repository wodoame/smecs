package com.smecs.service;

import com.smecs.entity.User;
import java.util.List;

public interface UserService {
    boolean registerUser(String username, String email, String password, String role);
    User authenticateUser(String usernameOrEmail, String password);
    List<User> getAllUsers();
    User findByUsername(String username);
    User findById(Long id);
    User findByEmail(String email);
    boolean usernameExists(String username);
    boolean emailExists(String email);
    boolean updateUser(User user);
    boolean deleteUser(int userId);
    String hashPassword(String password);
}
