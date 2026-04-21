package com.applicationtracker.study;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StudyReportPdfService {

    private final StudyCourseRepository courses;
    private final StudyTopicRepository topics;

    public StudyReportPdfService(StudyCourseRepository courses, StudyTopicRepository topics) {
        this.courses = courses;
        this.topics = topics;
    }

    public byte[] export(String email) {
        List<String> lines = new ArrayList<>();
        lines.add("Application Tracker - Study Report");
        lines.add(" ");

        Map<Long, List<StudyTopic>> topicsByCourse = topics.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email).stream()
                .collect(Collectors.groupingBy(topic -> topic.getCourse().getId()));
        var studyCourses = courses.findByUserEmailIgnoreCaseOrderByUpdatedAtDesc(email);
        studyCourses.forEach(course -> {
            lines.add(course.getName());
            if (course.getDescription() != null) {
                lines.add("Description: " + course.getDescription());
            }
            List<StudyTopic> courseTopics = topicsByCourse.getOrDefault(course.getId(), List.of());
            if (courseTopics.isEmpty()) {
                lines.add("No topics added.");
            }
            courseTopics.forEach(topic -> {
                lines.add("- " + topic.getTitle() + " [" + topic.getStatus() + "]");
                if (topic.getNotes() != null) {
                    lines.add("  Notes: " + topic.getNotes());
                }
            });
            lines.add(" ");
        });

        if (studyCourses.isEmpty()) {
            lines.add("No study courses found.");
        }
        return renderPdf(lines);
    }

    private byte[] renderPdf(List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 11 Tf\n50 760 Td\n14 TL\n");
        for (String line : lines) {
            content.append("(").append(escape(truncate(line))).append(") Tj\nT*\n");
        }
        content.append("ET\n");

        List<String> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + content.toString().getBytes(StandardCharsets.UTF_8).length + " >>\nstream\n" + content + "endstream"
        );

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.UTF_8).length);
            pdf.append(i + 1).append(" 0 obj\n").append(objects.get(i)).append("\nendobj\n");
        }
        int xrefOffset = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String truncate(String value) {
        return value.length() > 95 ? value.substring(0, 92) + "..." : value;
    }
}
