package com.finance.dashboard.controller;

import com.finance.dashboard.dto.request.UpdateUserRoleRequest;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "ADMIN-only user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users — paginated (ADMIN only)")
    public ResponseEntity<PagedResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(
                userService.getAllUsers(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (ADMIN only)")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change a user's role — cannot downgrade the only ADMIN")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate a user — cannot deactivate yourself or the only ADMIN")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(userService.updateStatus(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a user — cannot delete yourself or the only ADMIN")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
