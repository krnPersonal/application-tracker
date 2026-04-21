package com.applicationtracker.study;

import com.applicationtracker.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTopicService {

    private final StudyTopicRepository topics;
    private final StudyCourseRepository courses;

    public StudyTopicService(StudyTopicRepository topics, StudyCourseRepository courses) {
        this.topics = topics;
        this.courses = courses;
    }

    public List<StudyTopicResponse> list(String email) {
        return topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
                .map(StudyTopicResponse::from)
                .toList();
    }

    @Transactional
    public StudyTopicResponse create(String email, StudyTopicRequest request) {
        StudyCourse course = courseForUser(email, request.courseId());
        StudyTopic topic = new StudyTopic();
        topic.setUser(course.getUser());
        apply(topic, course, request);
        return StudyTopicResponse.from(topics.save(topic));
    }

    @Transactional
    public StudyTopicResponse update(String email, Long id, StudyTopicRequest request) {
        StudyTopic topic = topics.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Topic not found"));
        StudyCourse course = courseForUser(email, request.courseId());
        apply(topic, course, request);
        return StudyTopicResponse.from(topic);
    }

    @Transactional
    public void delete(String email, Long id) {
        StudyTopic topic = topics.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Topic not found"));
        topics.delete(topic);
    }

    private void apply(StudyTopic topic, StudyCourse course, StudyTopicRequest request) {
        topic.setCourse(course);
        topic.setTitle(request.title().trim());
        topic.setStatus(request.status());
        topic.setNotes(blankToNull(request.notes()));
    }

    private StudyCourse courseForUser(String email, Long courseId) {
        return courses.findByIdAndUserEmailIgnoreCase(courseId, email)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
