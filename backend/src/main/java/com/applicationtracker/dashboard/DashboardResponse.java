package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobStatus;
import java.time.LocalDate;
import java.util.Map;

public record DashboardResponse(long totalApplications, LocalDate from, LocalDate to, Map<JobStatus, Long> statusCounts) {}
