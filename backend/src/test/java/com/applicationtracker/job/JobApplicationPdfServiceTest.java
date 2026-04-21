package com.applicationtracker.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applicationtracker.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobApplicationPdfServiceTest {

    private final JobApplicationRepository applications = mock(JobApplicationRepository.class);
    private final JobApplicationPdfService service = new JobApplicationPdfService(applications);

    @Test
    void exportReturnsPdfWithEmptyStateWhenNoApplicationsExist() {
        when(applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("job@example.com")).thenReturn(List.of());

        String pdf = new String(service.export("job@example.com"), StandardCharsets.UTF_8);

        assertThat(pdf).startsWith("%PDF-1.4");
        assertThat(pdf).contains("Application Tracker - Job Applications");
        assertThat(pdf).contains("No job applications found.");
        assertThat(pdf).contains("%%EOF");
    }

    @Test
    void exportIncludesApplicationDetailsAndEscapesPdfText() {
        JobApplication application = application();
        when(applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc("job@example.com"))
                .thenReturn(List.of(application));

        String pdf = new String(service.export("job@example.com"), StandardCharsets.UTF_8);

        assertThat(pdf).contains("Acme \\(Remote\\) - Software Engineer");
        assertThat(pdf).contains("Status: INTERVIEWING");
        assertThat(pdf).contains("Applied: 2026-04-21");
        assertThat(pdf).contains("URL: https://example.com/jobs/1");
        assertThat(pdf).contains("Notes: Bring portfolio \\\\ resume");
    }

    private JobApplication application() {
        JobApplication application = new JobApplication();
        application.setUser(user());
        application.setCompany("Acme (Remote)");
        application.setTitle("Software Engineer");
        application.setStatus(JobStatus.INTERVIEWING);
        application.setAppliedDate(LocalDate.of(2026, 4, 21));
        application.setJobUrl("https://example.com/jobs/1");
        application.setNotes("Bring portfolio \\ resume");
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
