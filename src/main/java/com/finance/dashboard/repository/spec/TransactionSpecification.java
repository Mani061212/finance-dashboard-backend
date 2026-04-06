package com.finance.dashboard.repository.spec;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a JPA Specification for dynamic transaction filtering.
 * Performs a LEFT JOIN FETCH on 'user' to prevent N+1 queries when
 * TransactionResponse.from() accesses user fields. The fetch is skipped
 * on count queries (used internally by Spring Data for pagination totals).
 */
public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> filter(
            String type,
            String category,
            String search,
            LocalDate from,
            LocalDate to,
            String email,
            boolean isAdmin) {

        return (root, query, cb) -> {
            // Eager-fetch user only for data queries — skip for COUNT
            if (query.getResultType() != Long.class
                    && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // Role scope: non-admins see only their own records
            if (!isAdmin) {
                predicates.add(cb.equal(root.get("user").get("email"), email));
            }

            // Filter by type
            if (type != null && !type.isBlank()) {
                try {
                    predicates.add(cb.equal(
                            root.get("type"),
                            TransactionType.valueOf(type.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    predicates.add(cb.disjunction()); // no results for invalid type
                }
            }

            // Filter by category (partial, case-insensitive)
            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("category")),
                        "%" + category.trim().toLowerCase() + "%"));
            }

            // Keyword search: category OR description (description may be null — use COALESCE)
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("category")), pattern),
                        cb.like(cb.lower(
                                cb.coalesce(root.get("description"), "")), pattern)
                ));
            }

            // Date range
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
