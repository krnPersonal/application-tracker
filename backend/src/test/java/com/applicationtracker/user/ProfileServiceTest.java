package com.applicationtracker.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.BadRequestException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProfileServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ProfileService service = new ProfileService(users, passwordEncoder);

    @Test
    void updateTrimsNamesAndNormalizesEmail() {
        UserAccount user = user("old@example.com", "hash");
        when(users.findByEmailIgnoreCase("old@example.com")).thenReturn(Optional.of(user));
        when(users.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);

        UserSummary summary = service.update(
                "old@example.com",
                new UpdateProfileRequest("  New  ", "  Name  ", "  NEW@example.com  "));

        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(summary.email()).isEqualTo("new@example.com");
    }

    @Test
    void updateRejectsDuplicateEmail() {
        when(users.findByEmailIgnoreCase("old@example.com")).thenReturn(Optional.of(user("old@example.com", "hash")));
        when(users.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(
                "old@example.com",
                new UpdateProfileRequest("New", "Name", "taken@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        UserAccount user = user("user@example.com", "existing-hash");
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "existing-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(
                "user@example.com",
                new ChangePasswordRequest("wrong-password", "new-password")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    void changePasswordEncodesNewPassword() {
        UserAccount user = user("user@example.com", "existing-hash");
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "existing-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "existing-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword("user@example.com", new ChangePasswordRequest("old-password", "new-password"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    private UserAccount user(String email, String passwordHash) {
        UserAccount user = new UserAccount();
        user.setFirstName("Profile");
        user.setLastName("User");
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
