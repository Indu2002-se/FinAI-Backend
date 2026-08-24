package com.finai.backend.repository;

import com.finai.backend.entity.DetectedTransaction;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.DetectedTransactionStatus;
import com.finai.backend.entity.enums.DetectedTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DetectedTransactionRepository extends JpaRepository<DetectedTransaction, Long> {
    List<DetectedTransaction> findByUserOrderByTransactionDateDesc(User user);
    List<DetectedTransaction> findByUserAndStatusOrderByTransactionDateDesc(User user, DetectedTransactionStatus status);
    Optional<DetectedTransaction> findByIdAndUser(Long id, User user);
    Optional<DetectedTransaction> findByUserAndRawTextHash(User user, String rawTextHash);
    List<DetectedTransaction> findByUserAndAmountAndTransactionTypeAndTransactionDateBetween(
            User user, BigDecimal amount, DetectedTransactionType transactionType, LocalDateTime start, LocalDateTime end);
    long countByUserAndStatus(User user, DetectedTransactionStatus status);
}
