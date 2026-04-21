package com.applicationtracker.study;

import java.time.Instant;

public record StudyCourseResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    static StudyCourseResponse from(StudyCourse course) {
        return new StudyCourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getCreatedAt(),
                course.getUpdatedAt());
    }
}
