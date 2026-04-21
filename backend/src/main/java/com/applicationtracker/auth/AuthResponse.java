package com.applicationtracker.auth;

import com.applicationtracker.user.UserSummary;

public record AuthResponse(String token, UserSummary user) {}
