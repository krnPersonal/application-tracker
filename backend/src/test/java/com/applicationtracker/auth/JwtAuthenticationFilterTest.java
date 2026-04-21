package com.applicationtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterAuthenticatesValidBearerToken() throws ServletException, IOException {
        var userDetails = User.withUsername("user@example.com").password("encoded-password").roles("USER").build();
        MockHttpServletRequest request = requestWithAuthorization("Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtService.extractUsername("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isValid("valid-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(chain.getResponse()).isSameAs(response);
    }

    @Test
    void doFilterSkipsRequestsWithoutBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(anyString());
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void doFilterKeepsExistingAuthentication() throws ServletException, IOException {
        var existingAuthentication = new UsernamePasswordAuthenticationToken("existing@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);
        MockHttpServletRequest request = requestWithAuthorization("Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtService.extractUsername("valid-token")).thenReturn("user@example.com");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuthentication);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void doFilterClearsSecurityContextForInvalidToken() throws ServletException, IOException {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("existing@example.com", null));
        MockHttpServletRequest request = requestWithAuthorization("Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("Invalid token") {});

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    private MockHttpServletRequest requestWithAuthorization(String authorizationHeader) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authorizationHeader);
        return request;
    }
}
