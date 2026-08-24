package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.BudgetRequest;
import com.finai.backend.dto.response.BudgetResponse;
import com.finai.backend.dto.response.BudgetStatusResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface BudgetService {
    List<BudgetResponse> getBudgetsByMonth(String month, User user);
    BudgetResponse getBudgetById(Long id, User user);
    BudgetResponse createBudget(BudgetRequest request, User user);
    BudgetResponse updateBudget(Long id, BudgetRequest request, User user);
    void deleteBudget(Long id, User user);
    BudgetStatusResponse getBudgetStatus(String month, User user);
}
