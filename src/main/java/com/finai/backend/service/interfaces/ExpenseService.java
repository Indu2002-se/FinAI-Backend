package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.ExpenseRequest;
import com.finai.backend.dto.response.ExpenseResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface ExpenseService {
    List<ExpenseResponse> getAllExpenses(User user);
    ExpenseResponse getExpenseById(Long id, User user);
    ExpenseResponse createExpense(ExpenseRequest request, User user);
    ExpenseResponse updateExpense(Long id, ExpenseRequest request, User user);
    void deleteExpense(Long id, User user);
}
