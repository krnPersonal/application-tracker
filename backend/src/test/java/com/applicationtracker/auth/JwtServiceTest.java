package com.applicationtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generateTokenCanExtractUsername() {
        JwtService jwtService = new JwtService(SECRET, 15);
        UserDetails user = user("user@example.com");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
    }

    @Test
    void isValidReturnsTrueForMatchingNonExpiredUser() {
        JwtService jwtService = new JwtService(SECRET, 15);
        UserDetails user = user("user@example.com");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token, user)).isTrue();
    }

    @Test
    void isValidReturnsFalseForDifferentUsername() {
        JwtService jwtService = new JwtService(SECRET, 15);
        String token = jwtService.generateToken(user("user@example.com"));

        assertThat(jwtService.isValid(token, user("other@example.com"))).isFalse();
    }

    @Test
    void isValidReturnsFalseForExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, -1);
        UserDetails user = user("user@example.com");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token, user)).isFalse();
    }

    private UserDetails user(String email) {
        return User.withUsername(email).password("encoded-password").roles("USER").build();
    }
}
