package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required (VIEWER, ANALYST, or ADMIN)")
        Role role
) {}
