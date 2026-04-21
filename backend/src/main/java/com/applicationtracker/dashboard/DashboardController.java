package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobApplicationRepository;
import com.applicationtracker.job.JobStatus;
import java.security.Principal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final JobApplicationRepository applications;

    public DashboardController(JobApplicationRepository applications) {
        this.applications = applications;
    }

    @GetMapping
    DashboardResponse dashboard(Principal principal) {
        Map<JobStatus, Long> counts = applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(principal.getName()).stream()
                .collect(Collectors.groupingBy(application -> application.getStatus(), () -> new EnumMap<>(JobStatus.class), Collectors.counting()));
        Arrays.stream(JobStatus.values()).forEach(status -> counts.putIfAbsent(status, 0L));
        return new DashboardResponse(applications.countByUserEmailIgnoreCase(principal.getName()), counts);
    }
}
