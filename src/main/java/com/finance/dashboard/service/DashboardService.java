package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.dto.response.TrendResponse;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        // sumByType uses enum parameter — type-safe, no string literal risk
        BigDecimal income   = nullSafe(transactionRepository.sumByType(TransactionType.INCOME));
        BigDecimal expenses = nullSafe(transactionRepository.sumByType(TransactionType.EXPENSE));
        BigDecimal net      = income.subtract(expenses);
        long count          = transactionRepository.countActive();

        Map<String, BigDecimal> categoryTotals = transactionRepository.getCategoryTotals()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new));

        List<TransactionResponse> recent = transactionRepository
                .findRecentTransactions(PageRequest.of(0, 10))
                .map(TransactionResponse::from)
                .getContent();

        return new DashboardSummaryResponse(
                income, expenses, net, count, categoryTotals, recent);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions() {
        return transactionRepository
                .findRecentTransactions(PageRequest.of(0, 10))
                .map(TransactionResponse::from)
                .getContent();
    }

    @Transactional(readOnly = true)
    public TrendResponse getMonthlyTrends(int year) {
        return buildTrend("Monthly — " + year,
                transactionRepository.getMonthlyTrends(year));
    }

    @Transactional(readOnly = true)
    public TrendResponse getWeeklyTrends(int weeks) {
        return buildTrend("Weekly — last " + weeks + " weeks",
                transactionRepository.getWeeklyTrends(LocalDate.now().minusWeeks(weeks)));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Null-safe helper for aggregate queries that may return null on empty tables. */
    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Merges native-query rows [label, type, total] into TrendDataPoints.
     * Native queries return type as String ("INCOME"/"EXPENSE") — handled here.
     */
    private TrendResponse buildTrend(String period, List<Object[]> rows) {
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String     label = (String)     row[0];
            String     type  = (String)     row[1];
            BigDecimal total = (BigDecimal) row[2];
            map.computeIfAbsent(label, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if ("INCOME".equalsIgnoreCase(type)) {
                map.get(label)[0] = total;
            } else {
                map.get(label)[1] = total;
            }
        }
        List<TrendResponse.TrendDataPoint> dataPoints = map.entrySet().stream()
                .map(e -> new TrendResponse.TrendDataPoint(
                        e.getKey(),
                        e.getValue()[0],
                        e.getValue()[1],
                        e.getValue()[0].subtract(e.getValue()[1])))
                .collect(Collectors.toList());
        return new TrendResponse(period, dataPoints);
    }
}