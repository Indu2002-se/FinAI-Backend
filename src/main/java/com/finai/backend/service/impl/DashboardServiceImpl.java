package com.finai.backend.service.impl;

import com.finai.backend.dto.response.*;
import com.finai.backend.entity.*;
import com.finai.backend.repository.*;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsRepository savingsRepository;
    private final DebtRepository debtRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationAlertRepository notificationAlertRepository;
    private final UserProfileRepository userProfileRepository;
    private final AiService aiService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(User user) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        String currentMonth = now.format(MONTH_FORMATTER);

        // 1. Incomes & Expenses this month
        BigDecimal totalIncome = incomeRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        BigDecimal totalExpense = expenseRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);

        // Fallback to profile baseline if new user has 0 entered transactions
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0 && totalExpense.compareTo(BigDecimal.ZERO) == 0) {
            UserProfile p = userProfileRepository.findByUser(user).orElse(null);
            if (p != null) {
                if (p.getMonthlyIncome() != null) totalIncome = p.getMonthlyIncome();
                if (p.getMonthlyExpense() != null) totalExpense = p.getMonthlyExpense();
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        // 2. Savings Balance & Debt
        BigDecimal totalSavingsBalance = savingsRepository.sumTotalSavingsByUser(user);
        BigDecimal totalDebt = debtRepository.sumTotalActiveDebtByUser(user);

        // 3. Budget Status
        List<Budget> budgets = budgetRepository.findByUserAndBudgetMonth(user, currentMonth);
        BigDecimal budgetAllocated = BigDecimal.ZERO;
        BigDecimal budgetSpent = BigDecimal.ZERO;
        for (Budget b : budgets) {
            budgetAllocated = budgetAllocated.add(b.getAllocatedAmount());
            budgetSpent = budgetSpent.add(b.getSpentAmount());
        }
        double budgetUsagePct = 0.0;
        if (budgetAllocated.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsagePct = budgetSpent.divide(budgetAllocated, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        // 4. Recent Transactions
        List<ExpenseResponse> recentExpenses = expenseRepository.findByUserOrderByExpenseDateDesc(user, PageRequest.of(0, 5))
                .stream()
                .map(e -> ExpenseResponse.builder()
                        .id(e.getId())
                        .category(e.getCategory())
                        .amount(e.getAmount())
                        .expenseDate(e.getExpenseDate())
                        .description(e.getDescription())
                        .paymentMethod(e.getPaymentMethod())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<IncomeResponse> recentIncomes = incomeRepository.findByUserOrderByIncomeDateDesc(user, PageRequest.of(0, 5))
                .stream()
                .map(i -> IncomeResponse.builder()
                        .id(i.getId())
                        .source(i.getSource())
                        .category(i.getCategory())
                        .amount(i.getAmount())
                        .incomeDate(i.getIncomeDate())
                        .description(i.getDescription())
                        .isRecurring(i.getIsRecurring())
                        .createdAt(i.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 5. Alerts
        List<NotificationAlertResponse> alerts = notificationAlertRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(a -> NotificationAlertResponse.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .message(a.getMessage())
                        .alertType(a.getAlertType())
                        .isRead(a.getIsRead())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 6. AI Insights Summary
        FinancialRiskResponse risk = aiService.getLatestRiskPrediction(user);
        ExpenseForecastResponse fc = aiService.getLatestForecast(user);
        AiRecommendationResponse rec = aiService.getLatestRecommendation(user);

        String forecastSummary = "Projected 6-month expenditure trend: Stable";
        if (fc != null && fc.getTotal() != null && !fc.getTotal().isEmpty()) {
            BigDecimal firstMonth = fc.getTotal().get(0).getPredictedAmount();
            forecastSummary = String.format("Next month projected total: Rs.%s", firstMonth.toPlainString());
        }

        return DashboardResponse.builder()
                .userName(user.getFirstName() + " " + user.getLastName())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .totalSavingsBalance(totalSavingsBalance)
                .totalDebt(totalDebt)
                .monthlyBudgetAllocated(budgetAllocated)
                .monthlyBudgetSpent(budgetSpent)
                .budgetUsagePercentage(budgetUsagePct)
                .financialHealthScore(risk != null ? risk.getFinancialHealthScore() : BigDecimal.valueOf(75.0))
                .riskLevel(risk != null ? risk.getRiskLevel() : "Low Risk")
                .riskProbability(risk != null ? risk.getRiskProbability() : BigDecimal.valueOf(0.20))
                .topRiskDriver(risk != null ? risk.getTopDriverReadable() : "Expense-to-Income Ratio")
                .forecastSummary(forecastSummary)
                .latestRecommendation(rec != null ? rec.getRecommendationText() : "Maintain your disciplined savings habits.")
                .recentExpenses(recentExpenses)
                .recentIncomes(recentIncomes)
                .alerts(alerts)
                .build();
    }
}
