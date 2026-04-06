package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        Role role,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getRole(), user.getIsActive(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
