package com.finance.dashboard.repository;

import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {

    // ── Aggregates ─────────────────────────────────────────────────────────
    // Use enum parameter (not string literal) for type-safe JPQL comparison

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = :type AND t.isDeleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) " +
           "FROM Transaction t WHERE t.isDeleted = false " +
           "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryTotals();

    @Query(value = """
        SELECT TO_CHAR(transaction_date, 'YYYY-MM') AS month,
               type,
               CAST(SUM(amount) AS NUMERIC(15,2)) AS total
        FROM transactions
        WHERE is_deleted = false
          AND EXTRACT(YEAR FROM transaction_date) = :year
        GROUP BY TO_CHAR(transaction_date, 'YYYY-MM'), type
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrends(@Param("year") int year);

    @Query(value = """
        SELECT TO_CHAR(transaction_date, 'IYYY-IW') AS week,
               type,
               CAST(SUM(amount) AS NUMERIC(15,2)) AS total
        FROM transactions
        WHERE is_deleted = false
          AND transaction_date >= :from
        GROUP BY TO_CHAR(transaction_date, 'IYYY-IW'), type
        ORDER BY week
        """, nativeQuery = true)
    List<Object[]> getWeeklyTrends(@Param("from") LocalDate from);

    // ── Recent — @EntityGraph fetches user in same query (prevents N+1) ───
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false ORDER BY t.createdAt DESC")
    Page<Transaction> findRecentTransactions(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isDeleted = false")
    long countActive();
}
