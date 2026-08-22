package com.finai.backend.service.impl;

import com.finai.backend.dto.request.ExpenseRequest;
import com.finai.backend.dto.response.ExpenseResponse;
import com.finai.backend.entity.Budget;
import com.finai.backend.entity.Expense;
import com.finai.backend.entity.NotificationAlert;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.AlertType;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.BudgetRepository;
import com.finai.backend.repository.ExpenseRepository;
import com.finai.backend.repository.NotificationAlertRepository;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.service.interfaces.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationAlertRepository notificationAlertRepository;
    private final AiService aiService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses(User user) {
        return expenseRepository.findByUserOrderByExpenseDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id, User user) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return mapToResponse(expense);
    }

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, User user) {
        Expense expense = Expense.builder()
                .user(user)
                .category(request.getCategory())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .build();

        Expense saved = expenseRepository.save(expense);
        syncBudgetAndCheckAlerts(saved, user);
        triggerAiAnalysis(user);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, User user) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setPaymentMethod(request.getPaymentMethod());

        Expense updated = expenseRepository.save(expense);
        syncBudgetAndCheckAlerts(updated, user);
        triggerAiAnalysis(user);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, User user) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        expenseRepository.delete(expense);
        triggerAiAnalysis(user);
    }

    private void triggerAiAnalysis(User user) {
        try {
            aiService.runFullAnalysis(user);
        } catch (Exception e) {
            log.warn("AI analysis trigger failed after expense change: {}", e.getMessage());
        }
    }

    private void syncBudgetAndCheckAlerts(Expense expense, User user) {
        try {
            String currentMonth = expense.getExpenseDate().format(MONTH_FORMATTER);
            Optional<Budget> budgetOpt = budgetRepository.findByUserAndCategoryAndBudgetMonth(
                    user, expense.getCategory(), currentMonth);

            if (budgetOpt.isPresent()) {
                Budget budget = budgetOpt.get();
                LocalDate startDate = expense.getExpenseDate().withDayOfMonth(1);
                LocalDate endDate = expense.getExpenseDate().withDayOfMonth(expense.getExpenseDate().lengthOfMonth());
                BigDecimal totalCategorySpent = expenseRepository.sumAmountByUserAndCategoryAndDateRange(
                        user, expense.getCategory(), startDate, endDate);

                budget.setSpentAmount(totalCategorySpent);
                budgetRepository.save(budget);

                // Check for overspending alert
                if (budget.getAllocatedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (totalCategorySpent.compareTo(budget.getAllocatedAmount()) > 0) {
                        NotificationAlert alert = NotificationAlert.builder()
                                .user(user)
                                .title("Budget Exceeded: " + expense.getCategory())
                                .message(String.format("You have exceeded your %s budget for %s (Spent: %s, Allocated: %s)",
                                        expense.getCategory(), currentMonth, totalCategorySpent, budget.getAllocatedAmount()))
                                .alertType(AlertType.BUDGET_EXCEEDED)
                                .isRead(false)
                                .build();
                        notificationAlertRepository.save(alert);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error syncing budget on expense creation: {}", e.getMessage());
        }
    }

    public ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .paymentMethod(expense.getPaymentMethod())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
