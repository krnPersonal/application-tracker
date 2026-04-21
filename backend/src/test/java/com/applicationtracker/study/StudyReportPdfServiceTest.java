package com.applicationtracker.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applicationtracker.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class StudyReportPdfServiceTest {

    private final StudyCourseRepository courses = mock(StudyCourseRepository.class);
    private final StudyTopicRepository topics = mock(StudyTopicRepository.class);
    private final StudyReportPdfService service = new StudyReportPdfService(courses, topics);

    @Test
    void exportReturnsPdfWithEmptyStateWhenNoCoursesExist() {
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of());
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of());

        String pdf = new String(service.export("student@example.com"), StandardCharsets.UTF_8);

        assertThat(pdf).startsWith("%PDF-1.4");
        assertThat(pdf).contains("Application Tracker - Study Report");
        assertThat(pdf).contains("No study courses found.");
        assertThat(pdf).contains("%%EOF");
    }

    @Test
    void exportIncludesCoursesTopicsAndEscapesPdfText() {
        StudyCourse course = course(10L, "Java (Core)", "Spring \\ Boot");
        StudyTopic topic = topic(course, "Security", TopicStatus.DONE, "JWT (Bearer)");
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of(course));
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of(topic));

        String pdf = new String(service.export("student@example.com"), StandardCharsets.UTF_8);

        assertThat(pdf).contains("Java \\(Core\\)");
        assertThat(pdf).contains("Description: Spring \\\\ Boot");
        assertThat(pdf).contains("- Security [DONE]");
        assertThat(pdf).contains("  Notes: JWT \\(Bearer\\)");
    }

    @Test
    void exportShowsEmptyTopicMessageForCoursesWithoutTopics() {
        StudyCourse course = course(10L, "Java", null);
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of(course));
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com")).thenReturn(List.of());

        String pdf = new String(service.export("student@example.com"), StandardCharsets.UTF_8);

        assertThat(pdf).contains("Java");
        assertThat(pdf).contains("No topics added.");
    }

    private StudyCourse course(Long id, String name, String description) {
        StudyCourse course = new StudyCourse();
        ReflectionTestUtils.setField(course, "id", id);
        course.setUser(user());
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    private StudyTopic topic(StudyCourse course, String title, TopicStatus status, String notes) {
        StudyTopic topic = new StudyTopic();
        topic.setUser(course.getUser());
        topic.setCourse(course);
        topic.setTitle(title);
        topic.setStatus(status);
        topic.setNotes(notes);
        return topic;
    }

    private UserAccount user() {
        UserAccount user = new UserAccount();
        user.setFirstName("Study");
        user.setLastName("User");
        user.setEmail("student@example.com");
        user.setPasswordHash("hash");
        return user;
    }
}
