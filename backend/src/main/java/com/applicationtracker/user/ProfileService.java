package com.applicationtracker.user;

import com.applicationtracker.common.BadRequestException;
import com.applicationtracker.common.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @Transactional
    public UserSummary uploadResume(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Resume file is required");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Resume must be 5MB or smaller");
        }
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        try {
            user.setResumeFileName(file.getOriginalFilename());
            user.setResumeContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            user.setResumeData(file.getBytes());
            return UserSummary.from(user);
        } catch (IOException exception) {
            throw new BadRequestException("Could not read resume file");
        }
    }

    public ResumeDownload downloadResume(String email) {
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getResumeData() == null || user.getResumeFileName() == null) {
            throw new NotFoundException("Resume not found");
        }
        return new ResumeDownload(user.getResumeFileName(), user.getResumeContentType(), user.getResumeData());
    }
}
