package com.applicationtracker.study;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study/topics")
public class StudyTopicController {

    private final StudyTopicService service;

    public StudyTopicController(StudyTopicService service) {
        this.service = service;
    }

    @GetMapping
    List<StudyTopicResponse> list(Principal principal) {
        return service.list(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StudyTopicResponse create(Principal principal, @Valid @RequestBody StudyTopicRequest request) {
        return service.create(principal.getName(), request);
    }

    @PutMapping("/{id}")
    StudyTopicResponse update(Principal principal, @PathVariable Long id, @Valid @RequestBody StudyTopicRequest request) {
        return service.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(Principal principal, @PathVariable Long id) {
        service.delete(principal.getName(), id);
    }
}
