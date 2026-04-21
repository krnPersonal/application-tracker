package com.applicationtracker.user;

public record UserSummary(Long id, String firstName, String lastName, String email, Role role) {
    public static UserSummary from(UserAccount user) {
        return new UserSummary(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }
}
