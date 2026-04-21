package com.applicationtracker.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JobApplicationServiceTest {

    private final JobApplicationRepository applications = mock(JobApplicationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final JobApplicationService service = new JobApplicationService(applications, users);

    @Test
    void listFiltersByQueryAndStatusThenSortsByCompany() {
        JobApplication beta = application("Beta Co", "Backend Engineer", JobStatus.APPLIED, "Spring work");
        JobApplication alpha = application("Alpha Co", "Backend Engineer", JobStatus.APPLIED, "Java work");
        JobApplication hidden = application("Gamma Co", "Frontend Engineer", JobStatus.SAVED, "React work");
        when(applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("job@example.com"))
                .thenReturn(List.of(beta, hidden, alpha));

        List<JobApplicationResponse> responses = service.list(
                "job@example.com",
                "backend",
                JobStatus.APPLIED,
                "company",
                "asc");

        assertThat(responses)
                .extracting(JobApplicationResponse::company)
                .containsExactly("Alpha Co", "Beta Co");
    }

    @Test
    void createTrimsRequiredFieldsAndClearsBlankOptionalFields() {
        UserAccount user = user();
        when(users.findByEmailIgnoreCase("job@example.com")).thenReturn(Optional.of(user));
        when(applications.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create("job@example.com", new JobApplicationRequest(
                "  Acme  ",
                "  Software Engineer  ",
                JobStatus.SAVED,
                LocalDate.of(2026, 4, 21),
                "   ",
                "   "));

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(applications).save(captor.capture());
        JobApplication saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCompany()).isEqualTo("Acme");
        assertThat(saved.getTitle()).isEqualTo("Software Engineer");
        assertThat(saved.getJobUrl()).isNull();
        assertThat(saved.getNotes()).isNull();
    }

    @Test
    void updateAppliesChangesToOwnedApplication() {
        JobApplication application = application("Old Co", "Old Title", JobStatus.SAVED, "old notes");
        when(applications.findByIdAndUserEmailIgnoreCase(12L, "job@example.com")).thenReturn(Optional.of(application));

        JobApplicationResponse response = service.update("job@example.com", 12L, new JobApplicationRequest(
                "  New Co  ",
                "  New Title  ",
                JobStatus.INTERVIEWING,
                LocalDate.of(2026, 4, 21),
                " https://example.com/job ",
                " Updated notes "));

        assertThat(application.getCompany()).isEqualTo("New Co");
        assertThat(application.getTitle()).isEqualTo("New Title");
        assertThat(application.getStatus()).isEqualTo(JobStatus.INTERVIEWING);
        assertThat(application.getAppliedDate()).isEqualTo(LocalDate.of(2026, 4, 21));
        assertThat(application.getJobUrl()).isEqualTo("https://example.com/job");
        assertThat(application.getNotes()).isEqualTo("Updated notes");
        assertThat(response.company()).isEqualTo("New Co");
    }

    @Test
    void updateRejectsMissingApplication() {
        when(applications.findByIdAndUserEmailIgnoreCase(99L, "job@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("job@example.com", 99L, request()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Application not found");
    }

    @Test
    void deleteRemovesOwnedApplication() {
        JobApplication application = application("Acme", "Engineer", JobStatus.APPLIED, "notes");
        when(applications.findByIdAndUserEmailIgnoreCase(12L, "job@example.com")).thenReturn(Optional.of(application));

        service.delete("job@example.com", 12L);

        verify(applications).delete(application);
    }

    @Test
    void deleteRejectsMissingApplication() {
        when(applications.findByIdAndUserEmailIgnoreCase(99L, "job@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("job@example.com", 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Application not found");
    }

    private JobApplicationRequest request() {
        return new JobApplicationRequest(
                "Acme",
                "Software Engineer",
                JobStatus.APPLIED,
                LocalDate.of(2026, 4, 21),
                null,
                null);
    }

    private JobApplication application(String company, String title, JobStatus status, String notes) {
        JobApplication application = new JobApplication();
        application.setUser(user());
        application.setCompany(company);
        application.setTitle(title);
        application.setStatus(status);
        application.setNotes(notes);
        return application;
    }

    private UserAccount user() {
        UserAccount user = new UserAccount();
        user.setFirstName("Job");
        user.setLastName("User");
        user.setEmail("job@example.com");
        user.setPasswordHash("hash");
        return user;
    }
}
