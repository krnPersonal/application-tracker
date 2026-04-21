package com.applicationtracker.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StudyTopicServiceTest {

    private final StudyTopicRepository topics = mock(StudyTopicRepository.class);
    private final StudyCourseRepository courses = mock(StudyCourseRepository.class);
    private final StudyTopicService service = new StudyTopicService(topics, courses);

    @Test
    void listReturnsTopicsForAuthenticatedUser() {
        when(topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com"))
                .thenReturn(List.of(
                        topic("Security", TopicStatus.IN_PROGRESS, "JWT notes"),
                        topic("Testing", TopicStatus.DONE, null)));

        List<StudyTopicResponse> responses = service.list("student@example.com");

        assertThat(responses)
                .extracting(StudyTopicResponse::title)
                .containsExactly("Security", "Testing");
    }

    @Test
    void createRequiresCourseOwnedByAuthenticatedUser() {
        when(courses.findByIdAndUserEmailIgnoreCase(42L, "student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                "student@example.com",
                new StudyTopicRequest(42L, "Security", TopicStatus.IN_PROGRESS, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void createTrimsTopicAndCopiesCourseOwner() {
        StudyCourse course = course();
        when(courses.findByIdAndUserEmailIgnoreCase(42L, "student@example.com")).thenReturn(Optional.of(course));
        when(topics.save(any(StudyTopic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyTopicResponse response = service.create(
                "student@example.com",
                new StudyTopicRequest(42L, "  Spring Security  ", TopicStatus.IN_PROGRESS, "  JWT notes  "));

        ArgumentCaptor<StudyTopic> captor = ArgumentCaptor.forClass(StudyTopic.class);
        verify(topics).save(captor.capture());
        StudyTopic saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(course.getUser());
        assertThat(saved.getCourse()).isSameAs(course);
        assertThat(saved.getTitle()).isEqualTo("Spring Security");
        assertThat(saved.getNotes()).isEqualTo("JWT notes");
        assertThat(response.status()).isEqualTo(TopicStatus.IN_PROGRESS);
    }

    @Test
    void updateAppliesChangesToOwnedTopicAndCourse() {
        StudyTopic topic = topic("Old Topic", TopicStatus.NOT_STARTED, "old notes");
        StudyCourse newCourse = course();
        newCourse.setName("Security");
        when(topics.findByIdAndUserEmailIgnoreCase(12L, "student@example.com")).thenReturn(Optional.of(topic));
        when(courses.findByIdAndUserEmailIgnoreCase(42L, "student@example.com")).thenReturn(Optional.of(newCourse));

        StudyTopicResponse response = service.update(
                "student@example.com",
                12L,
                new StudyTopicRequest(42L, "  OAuth2  ", TopicStatus.DONE, "  token notes  "));

        assertThat(topic.getCourse()).isSameAs(newCourse);
        assertThat(topic.getTitle()).isEqualTo("OAuth2");
        assertThat(topic.getStatus()).isEqualTo(TopicStatus.DONE);
        assertThat(topic.getNotes()).isEqualTo("token notes");
        assertThat(response.title()).isEqualTo("OAuth2");
        assertThat(response.courseName()).isEqualTo("Security");
    }

    @Test
    void updateRejectsMissingTopic() {
        when(topics.findByIdAndUserEmailIgnoreCase(99L, "student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                "student@example.com",
                99L,
                new StudyTopicRequest(42L, "Security", TopicStatus.IN_PROGRESS, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Topic not found");
    }

    @Test
    void deleteRemovesOwnedTopic() {
        StudyTopic topic = topic("Security", TopicStatus.IN_PROGRESS, "notes");
        when(topics.findByIdAndUserEmailIgnoreCase(12L, "student@example.com")).thenReturn(Optional.of(topic));

        service.delete("student@example.com", 12L);

        verify(topics).delete(topic);
    }

    @Test
    void deleteRejectsMissingTopic() {
        when(topics.findByIdAndUserEmailIgnoreCase(99L, "student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("student@example.com", 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Topic not found");
    }

    private StudyTopic topic(String title, TopicStatus status, String notes) {
        StudyTopic topic = new StudyTopic();
        StudyCourse course = course();
        topic.setUser(course.getUser());
        topic.setCourse(course);
        topic.setTitle(title);
        topic.setStatus(status);
        topic.setNotes(notes);
        return topic;
    }

    private StudyCourse course() {
        UserAccount user = new UserAccount();
        user.setFirstName("Study");
        user.setLastName("User");
        user.setEmail("student@example.com");
        user.setPasswordHash("hash");

        StudyCourse course = new StudyCourse();
        course.setUser(user);
        course.setName("Java");
        return course;
    }
}
