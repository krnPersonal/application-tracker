package com.applicationtracker.job;

import java.time.Instant;
import java.time.LocalDate;

public record JobApplicationResponse(
        Long id,
        String company,
        String title,
        JobStatus status,
        LocalDate appliedDate,
        String jobUrl,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    static JobApplicationResponse from(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getTitle(),
                application.getStatus(),
                application.getAppliedDate(),
                application.getJobUrl(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
