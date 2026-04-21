package com.applicationtracker.job;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository applications;
    private final UserRepository users;

    public JobApplicationService(JobApplicationRepository applications, UserRepository users) {
        this.applications = applications;
        this.users = users;
    }

    public List<JobApplicationResponse> list(String email, String query, JobStatus status, String sort, String direction) {
        Comparator<JobApplication> comparator = comparator(sort);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        return applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
                .filter(application -> matchesQuery(application, query))
                .filter(application -> status == null || application.getStatus() == status)
                .sorted(comparator)
                .map(JobApplicationResponse::from)
                .toList();
    }

    @Transactional
    public JobApplicationResponse create(String email, JobApplicationRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("User not found"));
        JobApplication application = new JobApplication();
        application.setUser(user);
        apply(application, request);
        return JobApplicationResponse.from(applications.save(application));
    }

    @Transactional
    public JobApplicationResponse update(String email, Long id, JobApplicationRequest request) {
        JobApplication application = applications.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        apply(application, request);
        return JobApplicationResponse.from(application);
    }

    @Transactional
    public void delete(String email, Long id) {
        JobApplication application = applications.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        applications.delete(application);
    }

    private void apply(JobApplication application, JobApplicationRequest request) {
        application.setCompany(request.company().trim());
        application.setTitle(request.title().trim());
        application.setStatus(request.status());
        application.setAppliedDate(request.appliedDate());
        application.setJobUrl(blankToNull(request.jobUrl()));
        application.setNotes(blankToNull(request.notes()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean matchesQuery(JobApplication application, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return application.getCompany().toLowerCase(Locale.ROOT).contains(normalized)
                || application.getTitle().toLowerCase(Locale.ROOT).contains(normalized)
                || (application.getNotes() != null && application.getNotes().toLowerCase(Locale.ROOT).contains(normalized));
    }

    private Comparator<JobApplication> comparator(String sort) {
        Comparator<JobApplication> fallback = Comparator.comparing(JobApplication::getUpdatedAt);
        if (sort == null || sort.isBlank()) {
            return fallback;
        }
        return switch (sort) {
            case "company" -> Comparator.comparing(JobApplication::getCompany, String.CASE_INSENSITIVE_ORDER);
            case "title" -> Comparator.comparing(JobApplication::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "appliedDate" -> Comparator.comparing(
                    JobApplication::getAppliedDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(application -> application.getStatus().name());
            default -> fallback;
        };
    }
}
