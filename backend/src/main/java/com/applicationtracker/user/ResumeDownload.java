package com.applicationtracker.user;

public record ResumeDownload(String fileName, String contentType, byte[] data) {}
