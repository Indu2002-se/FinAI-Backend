package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.request.SavingsRequest;
import com.finai.backend.dto.response.SavingsGoalResponse;
import com.finai.backend.dto.response.SavingsResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface SavingsService {
    List<SavingsResponse> getAllSavings(User user);
    SavingsResponse getSavingsById(Long id, User user);
    SavingsResponse createSavings(SavingsRequest request, User user);
    SavingsResponse updateSavings(Long id, SavingsRequest request, User user);
    void deleteSavings(Long id, User user);

    List<SavingsGoalResponse> getAllGoals(User user);
    SavingsGoalResponse getGoalById(Long id, User user);
    SavingsGoalResponse createGoal(SavingsGoalRequest request, User user);
    SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, User user);
    void deleteGoal(Long id, User user);
}
