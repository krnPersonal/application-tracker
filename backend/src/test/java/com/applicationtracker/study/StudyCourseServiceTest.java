package com.applicationtracker.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StudyCourseServiceTest {

    private final StudyCourseRepository courses = mock(StudyCourseRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final StudyCourseService service = new StudyCourseService(courses, users);

    @Test
    void listReturnsCoursesForAuthenticatedUser() {
        when(courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("student@example.com"))
                .thenReturn(List.of(course("Java", "Spring"), course("SQL", null)));

        List<StudyCourseResponse> responses = service.list("student@example.com");

        assertThat(responses)
                .extracting(StudyCourseResponse::name)
                .containsExactly("Java", "SQL");
    }

    @Test
    void createTrimsFieldsAndAssignsAuthenticatedUser() {
        UserAccount user = user();
        when(users.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(courses.save(any(StudyCourse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyCourseResponse response = service.create(
                "student@example.com",
                new StudyCourseRequest("  Java  ", "  Spring Boot basics  "));

        ArgumentCaptor<StudyCourse> captor = ArgumentCaptor.forClass(StudyCourse.class);
        verify(courses).save(captor.capture());
        StudyCourse saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getName()).isEqualTo("Java");
        assertThat(saved.getDescription()).isEqualTo("Spring Boot basics");
        assertThat(response.name()).isEqualTo("Java");
    }

    @Test
    void updateAppliesChangesToOwnedCourse() {
        StudyCourse course = course("Old Name", "Old description");
        when(courses.findByIdAndUserEmailIgnoreCase(12L, "student@example.com")).thenReturn(Optional.of(course));

        StudyCourseResponse response = service.update(
                "student@example.com",
                12L,
                new StudyCourseRequest("  Java  ", "  Updated description  "));

        assertThat(course.getName()).isEqualTo("Java");
        assertThat(course.getDescription()).isEqualTo("Updated description");
        assertThat(response.name()).isEqualTo("Java");
        assertThat(response.description()).isEqualTo("Updated description");
    }

    @Test
    void updateRejectsCourseOwnedByAnotherUser() {
        when(courses.findByIdAndUserEmailIgnoreCase(99L, "student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                "student@example.com",
                99L,
                new StudyCourseRequest("Java", null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course not found");
    }

    @Test
    void deleteRemovesOwnedCourse() {
        StudyCourse course = course("Java", "Spring");
        when(courses.findByIdAndUserEmailIgnoreCase(12L, "student@example.com")).thenReturn(Optional.of(course));

        service.delete("student@example.com", 12L);

        verify(courses).delete(course);
    }

    @Test
    void deleteRejectsMissingCourse() {
        when(courses.findByIdAndUserEmailIgnoreCase(99L, "student@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("student@example.com", 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course not found");
    }

    private StudyCourse course(String name, String description) {
        StudyCourse course = new StudyCourse();
        course.setUser(user());
        course.setName(name);
        course.setDescription(description);
        return course;
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
