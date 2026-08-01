package com.finai.backend.repository;

import com.finai.backend.entity.WizardProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Wizard Profile repository
 * Handles database operations for WizardProfile entity
 */
@Repository
public interface WizardProfileRepository extends JpaRepository<WizardProfile, Long> {

    /**
     * Find wizard profile by user ID
     * @param userId the user ID
     * @return Optional containing the wizard profile if found
     */
    Optional<WizardProfile> findByUserId(Long userId);

    /**
     * Check if wizard profile exists for a user
     * @param userId the user ID
     * @return true if wizard profile exists, false otherwise
     */
    Boolean existsByUserId(Long userId);
}
