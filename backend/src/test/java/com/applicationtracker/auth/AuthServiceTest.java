package com.applicationtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.BadRequestException;
import com.applicationtracker.user.Role;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService service = new AuthService(
            users,
            passwordEncoder,
            authenticationManager,
            userDetailsService,
            jwtService);

    @Test
    void registerNormalizesEmailEncodesPasswordAndReturnsToken() {
        var details = User.withUsername("new@example.com").password("encoded-password").roles("USER").build();
        when(users.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername("new@example.com")).thenReturn(details);
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        AuthResponse response = service.register(new RegisterRequest(
                "  New  ",
                "  User  ",
                "  NEW@example.com  ",
                "password123"));

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(userCaptor.capture());
        UserAccount saved = userCaptor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("New");
        assertThat(saved.getLastName()).isEqualTo("User");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("new@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest(
                "Taken",
                "User",
                "taken@example.com",
                "password123")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void loginAuthenticatesAndReturnsToken() {
        UserAccount user = user("user@example.com");
        var details = User.withUsername("user@example.com").password("encoded-password").roles("USER").build();
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(details);
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        AuthResponse response = service.login(new AuthRequest("user@example.com", "password123"));

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user@example.com", "password123"));
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    @Test
    void loginRejectsMissingUserAfterAuthentication() {
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new AuthRequest("missing@example.com", "password123")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found");
    }

    private UserAccount user(String email) {
        UserAccount user = new UserAccount();
        user.setFirstName("Auth");
        user.setLastName("User");
        user.setEmail(email);
        user.setPasswordHash("encoded-password");
        user.setRole(Role.USER);
        return user;
    }
}
