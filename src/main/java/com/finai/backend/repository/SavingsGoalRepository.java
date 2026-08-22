package com.finai.backend.repository;

import com.finai.backend.entity.ChildProfile;
import com.finai.backend.entity.SavingsGoal;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
    List<SavingsGoal> findByUserAndStatus(User user, GoalStatus status);
    List<SavingsGoal> findByChildProfile(ChildProfile childProfile);
    List<SavingsGoal> findByChildProfileAndStatus(ChildProfile childProfile, GoalStatus status);
}
