package com.finai.backend.repository;

import com.finai.backend.entity.Debt;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUser(User user);
    List<Debt> findByUserAndStatus(User user, DebtStatus status);

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM Debt d WHERE d.user = :user AND d.status = 'ACTIVE'")
    BigDecimal sumTotalActiveDebtByUser(@Param("user") User user);
}
