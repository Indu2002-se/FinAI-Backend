package com.finai.backend.repository;

import com.finai.backend.entity.AiRecommendation;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
    Optional<AiRecommendation> findFirstByUserOrderByCreatedAtDesc(User user);
    List<AiRecommendation> findByUserOrderByCreatedAtDesc(User user);
}
