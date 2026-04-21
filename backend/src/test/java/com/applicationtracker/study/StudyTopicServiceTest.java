package com.applicationtracker.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StudyTopicServiceTest {

    private final StudyTopicRepository topics = mock(StudyTopicRepository.class);
    private final StudyCourseRepository courses = mock(StudyCourseRepository.class);
    private final StudyTopicService service = new StudyTopicService(topics, courses);

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
