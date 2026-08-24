package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.ChildProfileRequest;
import com.finai.backend.dto.request.QuizSubmitRequest;
import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.response.*;
import com.finai.backend.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface ChildService {
    // Parent actions
    List<ChildProfileResponse> getChildrenForParent(User parentUser);
    ChildProfileResponse getChildById(Long childId, User parentUser);
    ChildProfileResponse createChildProfile(ChildProfileRequest request, User parentUser);
    ChildProfileResponse updateChildProfile(Long childId, ChildProfileRequest request, User parentUser);
    void deleteChildProfile(Long childId, User parentUser);
    
    List<SavingsGoalResponse> getChildGoals(Long childId, User parentUser);
    SavingsGoalResponse createChildGoal(Long childId, SavingsGoalRequest request, User parentUser);
    SavingsGoalResponse updateChildGoal(Long childId, Long goalId, SavingsGoalRequest request, User parentUser);
    ChildProfileResponse depositChildSavings(Long childId, BigDecimal amount, User parentUser);

    // Child self-service
    ChildDashboardResponse getChildDashboard(User childOrParentUser);
    List<QuizResponse> getAvailableQuizzes(User user);
    QuizResponse getQuizById(Long quizId);
    QuizResultResponse submitQuizAttempt(Long quizId, QuizSubmitRequest request, User childOrParentUser);
    List<RewardResponse> getChildRewards(User childOrParentUser);
    List<QuizResultResponse> getChildQuizHistory(User childOrParentUser);
}
