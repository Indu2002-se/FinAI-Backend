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
    void deleteChildGoal(Long childId, Long goalId, User parentUser);
    SavingsGoalResponse addGoalProgress(Long childId, Long goalId, BigDecimal amountToAdd, User parentUser);
    ChildProfileResponse depositChildSavings(Long childId, BigDecimal amount, User parentUser);

    ChildDashboardResponse getChildDashboardForParent(Long childId, User parentUser);
    List<QuizResponse> getQuizzesForParent(Long childId, User parentUser);
    QuizResponse getQuizByIdForParent(Long childId, Long quizId, User parentUser);
    QuizResultResponse submitQuizAttemptForParent(Long childId, Long quizId, QuizSubmitRequest request, User parentUser);
    List<RewardResponse> getRewardsForParent(Long childId, User parentUser);
    List<QuizResultResponse> getProgressForParent(Long childId, User parentUser);

    // Child self-service
    ChildDashboardResponse getChildDashboard(User childUser);
    List<QuizResponse> getAvailableQuizzes(User user);
    QuizResponse getQuizById(Long quizId);
    QuizResultResponse submitQuizAttempt(Long quizId, QuizSubmitRequest request, User childUser);
    List<RewardResponse> getChildRewards(User childUser);
    List<QuizResultResponse> getChildQuizHistory(User childUser);
    SavingsGoalResponse addOwnGoalProgress(Long goalId, BigDecimal amountToAdd, User childUser);
}
