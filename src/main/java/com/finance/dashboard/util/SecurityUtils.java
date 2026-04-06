package com.finance.dashboard.util;

import com.finance.dashboard.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static String getCurrentUserEmail() {
        return getAuthentication().getName();
    }

    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public static boolean isAnalyst() {
        return hasRole("ROLE_ANALYST");
    }

    public static String getCurrentUserRole() {
        return getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> AppException.unauthorized("No role found"));
    }

    /** Safe hasRole — returns false (not throws) if unauthenticated. */
    private static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    /**
     * Returns the current Authentication or throws 401.
     * Guards against null, unauthenticated, and anonymous tokens
     * (Spring sets principal="anonymousUser" for unauthenticated requests).
     */
    private static Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw AppException.unauthorized("No authenticated user in security context");
        }
        return auth;
    }
}
