package com.applicationtracker.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 80) String password
) {}
