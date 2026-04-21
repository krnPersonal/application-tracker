package com.applicationtracker.job;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.List;
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

    public List<JobApplicationResponse> list(String email) {
        return applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
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
}
