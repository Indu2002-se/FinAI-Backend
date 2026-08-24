package com.finai.backend.repository;

import com.finai.backend.entity.TransactionDetectionSettings;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionDetectionSettingsRepository extends JpaRepository<TransactionDetectionSettings, Long> {
    Optional<TransactionDetectionSettings> findByUser(User user);
}
