package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        long totalTransactions,
        Map<String, BigDecimal> categoryTotals,
        List<TransactionResponse> recentTransactions
) {}
