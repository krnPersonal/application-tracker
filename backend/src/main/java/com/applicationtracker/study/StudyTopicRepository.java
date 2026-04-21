package com.applicationtracker.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTopicRepository extends JpaRepository<StudyTopic, Long> {
    List<StudyTopic> findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Optional<StudyTopic> findByIdAndUserEmailIgnoreCase(Long id, String email);
}
