package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.UpdateUserRoleRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PagedResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional
    public UserResponse updateRole(UUID id, UpdateUserRoleRequest request) {
        User user = findOrThrow(id);
        if (user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
            if (userRepository.countByRole(Role.ADMIN) <= 1) {
                throw AppException.badRequest(
                        "Cannot change role: this is the only ADMIN account");
            }
        }
        user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(UUID id, boolean active) {
        User user = findOrThrow(id);
        if (!active && user.getEmail().equals(SecurityUtils.getCurrentUserEmail())) {
            throw AppException.badRequest("You cannot deactivate your own account");
        }
        if (!active && user.getRole() == Role.ADMIN) {
            if (userRepository.countByRoleAndIsActive(Role.ADMIN, true) <= 1) {
                throw AppException.badRequest(
                        "Cannot deactivate: this is the only active ADMIN account");
            }
        }
        user.setIsActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findOrThrow(id);
        if (user.getEmail().equals(SecurityUtils.getCurrentUserEmail())) {
            throw AppException.badRequest("You cannot delete your own account");
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw AppException.badRequest("Cannot delete the only ADMIN account");
        }
        userRepository.delete(user);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
    }
}
