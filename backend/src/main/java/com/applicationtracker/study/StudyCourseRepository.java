package com.applicationtracker.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyCourseRepository extends JpaRepository<StudyCourse, Long> {
    List<StudyCourse> findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Optional<StudyCourse> findByIdAndUserEmailIgnoreCase(Long id, String email);
}
