package com.finai.backend.repository;

import com.finai.backend.entity.ChildProfile;
import com.finai.backend.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByChildProfileOrderByUnlockedAtDesc(ChildProfile childProfile);
}
