package com.applicationtracker.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudyTopicRequest(
        @NotNull Long courseId,
        @NotBlank @Size(max = 160) String title,
        @NotNull TopicStatus status,
        String notes
) {}
