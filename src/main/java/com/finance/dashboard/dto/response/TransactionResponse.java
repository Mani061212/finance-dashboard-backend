package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        LocalDate transactionDate,
        UUID userId,
        String userFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getAmount(), t.getType(),
                t.getCategory(), t.getDescription(),
                t.getTransactionDate(),
                t.getUser().getId(), t.getUser().getFullName(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
