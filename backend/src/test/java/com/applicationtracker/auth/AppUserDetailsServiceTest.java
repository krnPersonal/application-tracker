package com.applicationtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applicationtracker.user.Role;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class AppUserDetailsServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final AppUserDetailsService service = new AppUserDetailsService(users);

    @Test
    void loadUserByUsernameMapsStoredUserToSpringSecurityUser() {
        UserAccount user = new UserAccount();
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(Role.USER);
        when(users.findByEmailIgnoreCase("USER@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("USER@example.com");

        assertThat(details.getUsername()).isEqualTo("user@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsernameRejectsMissingUser() {
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
