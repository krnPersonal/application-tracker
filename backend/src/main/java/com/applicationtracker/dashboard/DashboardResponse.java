package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobStatus;
import com.applicationtracker.study.TopicStatus;
import java.time.LocalDate;
import java.util.Map;

public record DashboardResponse(
        long totalApplications,
        LocalDate from,
        LocalDate to,
        Map<JobStatus, Long> statusCounts,
        long totalCourses,
        long totalTopics,
        long completedTopics,
        long inProgressTopics,
        long notStartedTopics,
        int studyCompletionPercent,
        Map<TopicStatus, Long> topicStatusCounts
) {}
