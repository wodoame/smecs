package com.smecs.security;

import com.smecs.entity.User;
import com.smecs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmecsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public SmecsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail).orElse(null));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
        }
        if (user.getPasswordHash() == null) {
            throw new UsernameNotFoundException("User has no local credentials: " + usernameOrEmail);
        }

        String role = (user.getRole() != null && !user.getRole().isBlank())
                ? user.getRole()
                : "customer";
        String springRole = "ROLE_" + role.toUpperCase();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(springRole)))
                .build();
    }
}
