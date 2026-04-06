package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Analytics. " +
        "GET /recent is open to all roles. Summary and trends require ANALYST or ADMIN.")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Full summary: income, expenses, net balance, category totals, " +
                         "recent transactions. ANALYST and ADMIN only.")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/trends/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Monthly income vs expense breakdown. Defaults to current year.")
    public ResponseEntity<TrendResponse> getMonthlyTrends(
            @RequestParam(defaultValue = "0") int year) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(
                year == 0 ? LocalDate.now().getYear() : year));
    }

    @GetMapping("/trends/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Weekly income vs expense for the last N weeks.")
    public ResponseEntity<TrendResponse> getWeeklyTrends(
            @RequestParam(defaultValue = "12") int weeks) {
        return ResponseEntity.ok(dashboardService.getWeeklyTrends(weeks));
    }

    /**
     * Open to ALL authenticated roles (including VIEWER).
     * This is the primary entry point for VIEWERs per the assignment spec
     * ("Viewer: Can only view dashboard data").
     */
    @GetMapping("/recent")
    @Operation(summary = "10 most recent transactions. Open to all authenticated roles " +
                         "(VIEWER, ANALYST, ADMIN).")
    public ResponseEntity<List<TransactionResponse>> getRecent() {
        return ResponseEntity.ok(dashboardService.getRecentTransactions());
    }
}
