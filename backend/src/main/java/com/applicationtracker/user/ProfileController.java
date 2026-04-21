package com.applicationtracker.user;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    UserSummary update(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(principal.getName(), request);
    }
}
