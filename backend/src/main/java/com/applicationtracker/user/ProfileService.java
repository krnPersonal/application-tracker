package com.applicationtracker.user;

import com.applicationtracker.common.BadRequestException;
import com.applicationtracker.common.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSummary update(String currentEmail, UpdateProfileRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String requestedEmail = request.email().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(requestedEmail) && users.existsByEmailIgnoreCase(requestedEmail)) {
            throw new BadRequestException("Email is already registered");
        }
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(requestedEmail);
        return UserSummary.from(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }
}
