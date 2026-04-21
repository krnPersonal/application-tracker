package com.applicationtracker.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyCourseRequest(
        @NotBlank @Size(max = 160) String name,
        String description
) {}
