package com.finai.backend.repository;

import com.finai.backend.entity.Expense;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserOrderByExpenseDateDesc(User user);
    Page<Expense> findByUserOrderByExpenseDateDesc(User user, Pageable pageable);
    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Expense> findByUserAndCategoryAndExpenseDateBetween(User user, ExpenseCategory category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndCategoryAndDateRange(@Param("user") User user, @Param("category") ExpenseCategory category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalAmountByUser(@Param("user") User user);
}
