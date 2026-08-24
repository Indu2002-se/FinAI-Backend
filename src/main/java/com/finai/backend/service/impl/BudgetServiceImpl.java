package com.finai.backend.service.impl;

import com.finai.backend.dto.request.BudgetRequest;
import com.finai.backend.dto.response.BudgetResponse;
import com.finai.backend.dto.response.BudgetStatusResponse;
import com.finai.backend.entity.Budget;
import com.finai.backend.entity.User;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.BudgetRepository;
import com.finai.backend.repository.ExpenseRepository;
import com.finai.backend.service.interfaces.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByMonth(String month, User user) {
        String targetMonth = (month != null && !month.isBlank()) ? month : LocalDate.now().format(MONTH_FORMATTER);
        return budgetRepository.findByUserAndBudgetMonth(user, targetMonth)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id, User user) {
        Budget b = budgetRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        return mapToResponse(b);
    }

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request, User user) {
        Optional<Budget> existing = budgetRepository.findByUserAndCategoryAndBudgetMonth(
                user, request.getCategory(), request.getBudgetMonth());

        if (existing.isPresent()) {
            throw new BadRequestException("Budget for category " + request.getCategory() + " already exists for month " + request.getBudgetMonth());
        }

        // Calculate initial spent amount from existing expenses in that month
        LocalDate start = LocalDate.parse(request.getBudgetMonth() + "-01");
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, request.getCategory(), start, end);

        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .budgetMonth(request.getBudgetMonth())
                .allocatedAmount(request.getAllocatedAmount())
                .spentAmount(spent)
                .alertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : new BigDecimal("80.00"))
                .build();

        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request, User user) {
        Budget budget = budgetRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));

        budget.setCategory(request.getCategory());
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setAllocatedAmount(request.getAllocatedAmount());
        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }

        // Recalculate spent
        LocalDate start = LocalDate.parse(request.getBudgetMonth() + "-01");
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, request.getCategory(), start, end);
        budget.setSpentAmount(spent);

        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void deleteBudget(Long id, User user) {
        Budget budget = budgetRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus(String month, User user) {
        String targetMonth = (month != null && !month.isBlank()) ? month : LocalDate.now().format(MONTH_FORMATTER);
        List<Budget> budgets = budgetRepository.findByUserAndBudgetMonth(user, targetMonth);

        BigDecimal totalAllocated = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Budget b : budgets) {
            totalAllocated = totalAllocated.add(b.getAllocatedAmount());
            totalSpent = totalSpent.add(b.getSpentAmount());
        }

        BigDecimal totalRemaining = totalAllocated.subtract(totalSpent);
        double usagePct = 0.0;
        if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
            usagePct = totalSpent.divide(totalAllocated, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        List<BudgetResponse> categoryResponses = budgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return BudgetStatusResponse.builder()
                .month(targetMonth)
                .totalAllocated(totalAllocated)
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .overallUsagePercentage(usagePct)
                .categoryBudgets(categoryResponses)
                .build();
    }

    public BudgetResponse mapToResponse(Budget b) {
        BigDecimal remaining = b.getAllocatedAmount().subtract(b.getSpentAmount());
        double usage = 0.0;
        if (b.getAllocatedAmount().compareTo(BigDecimal.ZERO) > 0) {
            usage = b.getSpentAmount().divide(b.getAllocatedAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }
        boolean isExceeded = b.getSpentAmount().compareTo(b.getAllocatedAmount()) > 0;

        return BudgetResponse.builder()
                .id(b.getId())
                .category(b.getCategory())
                .budgetMonth(b.getBudgetMonth())
                .allocatedAmount(b.getAllocatedAmount())
                .spentAmount(b.getSpentAmount())
                .remainingAmount(remaining)
                .usagePercentage(usage)
                .isExceeded(isExceeded)
                .alertThreshold(b.getAlertThreshold())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
