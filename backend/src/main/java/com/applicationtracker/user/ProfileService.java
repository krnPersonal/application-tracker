package com.applicationtracker.user;

import com.applicationtracker.common.BadRequestException;
import com.applicationtracker.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository users;

    public ProfileService(UserRepository users) {
        this.users = users;
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
}
