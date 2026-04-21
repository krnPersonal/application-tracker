package com.applicationtracker.dashboard;

import com.applicationtracker.job.JobStatus;
import java.util.Map;

public record DashboardResponse(long totalApplications, Map<JobStatus, Long> statusCounts) {}
