package com.finai.backend.repository;

import com.finai.backend.entity.FinancialPrediction;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialPredictionRepository extends JpaRepository<FinancialPrediction, Long> {
    Optional<FinancialPrediction> findFirstByUserOrderByCreatedAtDesc(User user);
    List<FinancialPrediction> findByUserOrderByCreatedAtDesc(User user);
}
