package com.applicationtracker.study;

import java.time.Instant;

public record StudyTopicResponse(
        Long id,
        Long courseId,
        String courseName,
        String title,
        TopicStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    static StudyTopicResponse from(StudyTopic topic) {
        return new StudyTopicResponse(
                topic.getId(),
                topic.getCourse().getId(),
                topic.getCourse().getName(),
                topic.getTitle(),
                topic.getStatus(),
                topic.getNotes(),
                topic.getCreatedAt(),
                topic.getUpdatedAt());
    }
}
