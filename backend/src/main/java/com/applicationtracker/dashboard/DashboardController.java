package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobApplicationRepository;
import com.applicationtracker.job.JobStatus;
import com.applicationtracker.study.StudyCourseRepository;
import com.applicationtracker.study.StudyTopicRepository;
import com.applicationtracker.study.TopicStatus;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final JobApplicationRepository applications;
    private final StudyCourseRepository courses;
    private final StudyTopicRepository topics;

    public DashboardController(JobApplicationRepository applications, StudyCourseRepository courses, StudyTopicRepository topics) {
        this.applications = applications;
        this.courses = courses;
        this.topics = topics;
    }

    @GetMapping
    DashboardResponse dashboard(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var filteredApplications = applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(principal.getName()).stream()
                .filter(application -> from == null || (application.getAppliedDate() != null && !application.getAppliedDate().isBefore(from)))
                .filter(application -> to == null || (application.getAppliedDate() != null && !application.getAppliedDate().isAfter(to)))
                .toList();
        Map<JobStatus, Long> counts = filteredApplications.stream()
                .collect(Collectors.groupingBy(application -> application.getStatus(), () -> new EnumMap<>(JobStatus.class), Collectors.counting()));
        Arrays.stream(JobStatus.values()).forEach(status -> counts.putIfAbsent(status, 0L));

        var studyTopics = topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(principal.getName());
        Map<TopicStatus, Long> topicCounts = studyTopics.stream()
                .collect(Collectors.groupingBy(topic -> topic.getStatus(), () -> new EnumMap<>(TopicStatus.class), Collectors.counting()));
        Arrays.stream(TopicStatus.values()).forEach(status -> topicCounts.putIfAbsent(status, 0L));
        long totalTopics = studyTopics.size();
        long completedTopics = topicCounts.get(TopicStatus.DONE);
        int completionPercent = totalTopics == 0 ? 0 : (int) Math.round((completedTopics * 100.0) / totalTopics);

        return new DashboardResponse(
                filteredApplications.size(),
                from,
                to,
                counts,
                courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(principal.getName()).size(),
                totalTopics,
                completedTopics,
                topicCounts.get(TopicStatus.IN_PROGRESS),
                topicCounts.get(TopicStatus.NOT_STARTED),
                completionPercent,
                topicCounts);
    }
}
