package com.finai.backend.repository;

import com.finai.backend.entity.Income;
import com.finai.backend.entity.User;
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
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserOrderByIncomeDateDesc(User user);
    Page<Income> findByUserOrderByIncomeDateDesc(User user, Pageable pageable);
    List<Income> findByUserAndIncomeDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.incomeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user")
    BigDecimal sumTotalAmountByUser(@Param("user") User user);
}
