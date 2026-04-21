package com.applicationtracker.study;

import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study")
public class StudyReportController {

    private final StudyReportPdfService pdfService;

    public StudyReportController(StudyReportPdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/report.pdf")
    ResponseEntity<byte[]> exportPdf(Principal principal) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"study-report.pdf\"")
                .body(pdfService.export(principal.getName()));
    }
}
