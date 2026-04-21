package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobApplicationRepository;
import com.applicationtracker.job.JobStatus;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    DashboardResponse dashboard(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var filteredApplications = applications.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(principal.getName()).stream()
                .filter(application -> from == null || (application.getAppliedDate() != null && !application.getAppliedDate().isBefore(from)))
                .filter(application -> to == null || (application.getAppliedDate() != null && !application.getAppliedDate().isAfter(to)))
                .toList();
        Map<JobStatus, Long> counts = filteredApplications.stream()
                .collect(Collectors.groupingBy(application -> application.getStatus(), () -> new EnumMap<>(JobStatus.class), Collectors.counting()));
        Arrays.stream(JobStatus.values()).forEach(status -> counts.putIfAbsent(status, 0L));
        return new DashboardResponse(filteredApplications.size(), from, to, counts);
    }
}
