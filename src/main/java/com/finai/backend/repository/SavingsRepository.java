package com.finai.backend.repository;

import com.finai.backend.entity.Savings;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {
    List<Savings> findByUser(User user);

    @Query("SELECT COALESCE(SUM(s.currentBalance), 0) FROM Savings s WHERE s.user = :user")
    BigDecimal sumTotalSavingsByUser(@Param("user") User user);
}
