package com.applicationtracker.study;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserAccount;
import com.applicationtracker.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyCourseService {

    private final StudyCourseRepository courses;
    private final UserRepository users;

    public StudyCourseService(StudyCourseRepository courses, UserRepository users) {
        this.courses = courses;
        this.users = users;
    }

    public List<StudyCourseResponse> list(String email) {
        return courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
                .map(StudyCourseResponse::from)
                .toList();
    }

    @Transactional
    public StudyCourseResponse create(String email, StudyCourseRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("User not found"));
        StudyCourse course = new StudyCourse();
        course.setUser(user);
        apply(course, request);
        return StudyCourseResponse.from(courses.save(course));
    }

    @Transactional
    public StudyCourseResponse update(String email, Long id, StudyCourseRequest request) {
        StudyCourse course = courses.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Course not found"));
        apply(course, request);
        return StudyCourseResponse.from(course);
    }

    @Transactional
    public void delete(String email, Long id) {
        StudyCourse course = courses.findByIdAndUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException("Course not found"));
        courses.delete(course);
    }

    private void apply(StudyCourse course, StudyCourseRequest request) {
        course.setName(request.name().trim());
        course.setDescription(blankToNull(request.description()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
