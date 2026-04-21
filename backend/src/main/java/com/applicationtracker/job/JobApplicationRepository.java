package com.applicationtracker.job;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Optional<JobApplication> findByIdAndUserEmailIgnoreCase(Long id, String email);

    long countByUserEmailIgnoreCase(String email);
}
