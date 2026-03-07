package com.smecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String passwordHash;
    private String role;
    private java.sql.Timestamp createdAt;

    /** "local" for username/password users, "google" for OAuth2 users. */
    @Column(nullable = false)
    private String provider = "local";

    /** The OAuth2 provider's subject identifier (Google "sub" claim). */
    private String providerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
}
