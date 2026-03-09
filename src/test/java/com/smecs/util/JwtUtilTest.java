package com.smecs.util;

import com.smecs.entity.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private static final String TEST_SECRET = "test_secret_key_which_is_long_enough_for_hs256";
    private final JwtUtil jwtUtil = new JwtUtil(TEST_SECRET);

    @Test
    public void testGenerateAndValidateToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole("CUSTOMER");

        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));

        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertEquals("CUSTOMER", jwtUtil.extractRole(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
    }

    @Test
    public void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.string"));
    }
}
