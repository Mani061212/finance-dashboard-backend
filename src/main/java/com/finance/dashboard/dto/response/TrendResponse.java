package com.finance.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TrendResponse(String period, List<TrendDataPoint> data) {
    public record TrendDataPoint(
            String label,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal net
    ) {}
}
