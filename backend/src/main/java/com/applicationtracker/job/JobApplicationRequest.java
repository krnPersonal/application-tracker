package com.applicationtracker.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record JobApplicationRequest(
        @NotBlank @Size(max = 160) String company,
        @NotBlank @Size(max = 160) String title,
        @NotNull JobStatus status,
        LocalDate appliedDate,
        @Size(max = 500) String jobUrl,
        String notes
) {}
