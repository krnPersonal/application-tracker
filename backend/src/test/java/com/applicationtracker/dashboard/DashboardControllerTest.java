package com.applicationtracker.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applicationtracker.job.JobApplication;
import com.applicationtracker.job.JobApplicationRepository;
import com.applicationtracker.job.JobStatus;
import com.applicationtracker.study.StudyCourse;
import com.applicationtracker.study.StudyCourseRepository;
import com.applicationtracker.study.StudyTopic;
import com.applicationtracker.study.StudyTopicRepository;
import com.applicationtracker.study.TopicStatus;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardControllerTest {

    private final JobApplicationRepository applications = mock(JobApplicationRepository.class);
    private final StudyCourseRepository courses = mock(StudyCourseRepository.class);
    private final StudyTopicRepository topics = mock(StudyTopicRepository.class);
    private final DashboardController controller = new DashboardController(applications, courses, topics);
    private final Principal principal = () -> "user@example.com";

    @Test
    void dashboardFiltersApplicationsByAppliedDateRangeAndCountsStatuses() {
        when(applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com")).thenReturn(List.of(
                application(JobStatus.APPLIED, LocalDate.of(2026, 1, 10)),
                application(JobStatus.INTERVIEWING, LocalDate.of(2026, 1, 15)),
                application(JobStatus.OFFER, LocalDate.of(2026, 2, 1)),
                application(JobStatus.SAVED, null)));
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com")).thenReturn(List.of());
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com")).thenReturn(List.of());

        DashboardResponse response = controller.dashboard(
                principal,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31));

        assertThat(response.totalApplications()).isEqualTo(2);
        assertThat(response.from()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.to()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(response.statusCounts()).containsEntry(JobStatus.APPLIED, 1L);
        assertThat(response.statusCounts()).containsEntry(JobStatus.INTERVIEWING, 1L);
        assertThat(response.statusCounts()).containsEntry(JobStatus.OFFER, 0L);
        assertThat(response.statusCounts()).containsEntry(JobStatus.SAVED, 0L);
        assertThat(response.statusCounts()).containsEntry(JobStatus.REJECTED, 0L);
        assertThat(response.statusCounts()).containsEntry(JobStatus.WITHDRAWN, 0L);
    }

    @Test
    void dashboardCountsStudyProgressAndCompletionPercentage() {
        when(applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com")).thenReturn(List.of());
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com"))
                .thenReturn(List.of(new StudyCourse(), new StudyCourse()));
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("user@example.com")).thenReturn(List.of(
                topic(TopicStatus.DONE),
                topic(TopicStatus.DONE),
                topic(TopicStatus.IN_PROGRESS),
                topic(TopicStatus.NOT_STARTED)));

        DashboardResponse response = controller.dashboard(principal, null, null);

        assertThat(response.totalCourses()).isEqualTo(2);
        assertThat(response.totalTopics()).isEqualTo(4);
        assertThat(response.completedTopics()).isEqualTo(2);
        assertThat(response.inProgressTopics()).isEqualTo(1);
        assertThat(response.notStartedTopics()).isEqualTo(1);
        assertThat(response.studyCompletionPercent()).isEqualTo(50);
        assertThat(response.topicStatusCounts()).containsEntry(TopicStatus.DONE, 2L);
        assertThat(response.topicStatusCounts()).containsEntry(TopicStatus.IN_PROGRESS, 1L);
        assertThat(response.topicStatusCounts()).containsEntry(TopicStatus.NOT_STARTED, 1L);
    }

    private JobApplication application(JobStatus status, LocalDate appliedDate) {
        JobApplication application = new JobApplication();
        application.setStatus(status);
        application.setAppliedDate(appliedDate);
        return application;
    }

    private StudyTopic topic(TopicStatus status) {
        StudyTopic topic = new StudyTopic();
        topic.setStatus(status);
        return topic;
    }
}
