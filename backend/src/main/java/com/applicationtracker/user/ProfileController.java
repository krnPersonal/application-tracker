package com.applicationtracker.user;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(principal.getName(), request);
    }

    @PutMapping(path = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserSummary uploadResume(Principal principal, @RequestParam("file") MultipartFile file) {
        return profileService.uploadResume(principal.getName(), file);
    }

    @GetMapping("/resume")
    ResponseEntity<byte[]> downloadResume(Principal principal) {
        ResumeDownload resume = profileService.downloadResume(principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.fileName() + "\"")
                .body(resume.data());
    }
}
