package com.smecs.repository;

import com.smecs.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(Instant cutoff);
}
