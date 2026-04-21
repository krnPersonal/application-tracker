package com.applicationtracker.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
