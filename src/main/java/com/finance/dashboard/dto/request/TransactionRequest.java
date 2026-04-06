package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 13, fraction = 2,
                message = "Amount: max 13 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        TransactionType type,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate
) {}