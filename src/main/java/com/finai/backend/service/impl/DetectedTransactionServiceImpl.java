package com.finai.backend.service.impl;

import com.finai.backend.dto.request.ConfirmTransactionRequest;
import com.finai.backend.dto.request.DetectedTransactionRequest;
import com.finai.backend.dto.request.DetectionSettingsRequest;
import com.finai.backend.dto.response.DetectedTransactionResponse;
import com.finai.backend.dto.response.DetectionSettingsResponse;
import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.*;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.*;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.service.interfaces.DetectedTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectedTransactionServiceImpl implements DetectedTransactionService {

    private final DetectedTransactionRepository detectedTransactionRepository;
    private final TransactionDetectionSettingsRepository settingsRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationAlertRepository notificationAlertRepository;
    private final AiService aiService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional
    public DetectedTransactionResponse recordDetectedTransaction(DetectedTransactionRequest request, User user) {
        // Duplicate check 1: Raw text hash
        if (request.getRawTextHash() != null && !request.getRawTextHash().isBlank()) {
            Optional<DetectedTransaction> existingByHash = detectedTransactionRepository.findByUserAndRawTextHash(user, request.getRawTextHash());
            if (existingByHash.isPresent()) {
                log.info("Duplicate transaction detected by hash for user {}: {}", user.getId(), request.getRawTextHash());
                DetectedTransaction dt = existingByHash.get();
                dt.setStatus(DetectedTransactionStatus.DUPLICATE);
                return mapToResponse(detectedTransactionRepository.save(dt));
            }
        }

        LocalDateTime date = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now();

        // Duplicate check 2: Time window (+/- 15 mins) with same amount and transaction type
        LocalDateTime startWindow = date.minusMinutes(15);
        LocalDateTime endWindow = date.plusMinutes(15);
        List<DetectedTransaction> duplicates = detectedTransactionRepository.findByUserAndAmountAndTransactionTypeAndTransactionDateBetween(
                user, request.getAmount(), request.getTransactionType(), startWindow, endWindow);

        DetectedTransactionStatus initialStatus = duplicates.isEmpty()
                ? DetectedTransactionStatus.PENDING
                : DetectedTransactionStatus.DUPLICATE;

        if (initialStatus == DetectedTransactionStatus.DUPLICATE) {
            log.info("Duplicate transaction detected by time window for user {}: amount={}, type={}", user.getId(), request.getAmount(), request.getTransactionType());
        }

        DetectedTransaction transaction = DetectedTransaction.builder()
                .user(user)
                .sourceType(request.getSourceType())
                .sourceApp(request.getSourceApp())
                .sourceSender(request.getSourceSender())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .merchant(request.getMerchant())
                .accountReference(request.getAccountReference())
                .transactionDate(date)
                .reference(request.getReference())
                .rawTextHash(request.getRawTextHash())
                .confidence(request.getConfidence())
                .status(initialStatus)
                .suggestedCategory(request.getSuggestedCategory())
                .build();

        DetectedTransaction saved = detectedTransactionRepository.save(transaction);

        // Detailed console log for developer / terminal monitoring
        log.info("\n" +
                "╔════════════════════════════════════════════════════════════════════════════╗\n" +
                "║ 📩 [FINAI AUTO-TRANSACTION DETECTED]                                      ║\n" +
                "╠════════════════════════════════════════════════════════════════════════════╣\n" +
                "║  👤 User ID        : {} ({})\n" +
                "║  📱 Source Type    : {}\n" +
                "║  🏦 Sender / App   : {}\n" +
                "║  💰 Amount         : LKR {}\n" +
                "║  📊 Type           : {}\n" +
                "║  🏪 Merchant / Pay : {}\n" +
                "║  💳 Account Ref    : {}\n" +
                "║  🔖 Reference / Txn: {}\n" +
                "║  🏷️ Suggested Cat  : {}\n" +
                "║  🎯 Confidence     : {}%\n" +
                "║  🚦 Saved Status   : {}\n" +
                "╚════════════════════════════════════════════════════════════════════════════╝",
                user.getId(), user.getEmail(),
                saved.getSourceType(),
                saved.getSourceSender() != null ? saved.getSourceSender() : (saved.getSourceApp() != null ? saved.getSourceApp() : "Unknown"),
                saved.getAmount(),
                saved.getTransactionType(),
                saved.getMerchant() != null ? saved.getMerchant() : "N/A",
                saved.getAccountReference() != null ? saved.getAccountReference() : "N/A",
                saved.getReference() != null ? saved.getReference() : "N/A",
                saved.getSuggestedCategory() != null ? saved.getSuggestedCategory() : "N/A",
                saved.getConfidence() != null ? saved.getConfidence().multiply(BigDecimal.valueOf(100)).intValue() : 0,
                saved.getStatus());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<DetectedTransactionResponse> recordBatchDetectedTransactions(List<DetectedTransactionRequest> requests, User user) {
        List<DetectedTransactionResponse> responses = new ArrayList<>();
        for (DetectedTransactionRequest req : requests) {
            responses.add(recordDetectedTransaction(req, user));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetectedTransactionResponse> getPendingTransactions(User user) {
        return detectedTransactionRepository.findByUserAndStatusOrderByTransactionDateDesc(user, DetectedTransactionStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetectedTransactionResponse> getAllDetectedTransactions(User user) {
        return detectedTransactionRepository.findByUserOrderByTransactionDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DetectedTransactionResponse getDetectedTransactionById(Long id, User user) {
        DetectedTransaction dt = detectedTransactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("DetectedTransaction", "id", id));
        return mapToResponse(dt);
    }

    @Override
    @Transactional
    public DetectedTransactionResponse updateDetectedTransaction(Long id, DetectedTransactionRequest request, User user) {
        DetectedTransaction dt = detectedTransactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("DetectedTransaction", "id", id));

        if (dt.getStatus() == DetectedTransactionStatus.CONFIRMED) {
            throw new BadRequestException("Cannot update an already confirmed transaction");
        }

        if (request.getAmount() != null) dt.setAmount(request.getAmount());
        if (request.getTransactionType() != null) dt.setTransactionType(request.getTransactionType());
        if (request.getMerchant() != null) dt.setMerchant(request.getMerchant());
        if (request.getAccountReference() != null) dt.setAccountReference(request.getAccountReference());
        if (request.getTransactionDate() != null) dt.setTransactionDate(request.getTransactionDate());
        if (request.getReference() != null) dt.setReference(request.getReference());
        if (request.getSuggestedCategory() != null) dt.setSuggestedCategory(request.getSuggestedCategory());

        return mapToResponse(detectedTransactionRepository.save(dt));
    }

    @Override
    @Transactional
    public DetectedTransactionResponse confirmTransaction(Long id, ConfirmTransactionRequest request, User user) {
        DetectedTransaction dt = detectedTransactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("DetectedTransaction", "id", id));

        if (dt.getStatus() == DetectedTransactionStatus.CONFIRMED) {
            throw new BadRequestException("Transaction is already confirmed");
        }

        BigDecimal finalAmount = (request != null && request.getAmount() != null) ? request.getAmount() : dt.getAmount();
        LocalDate txnDate = (request != null && request.getTransactionDate() != null)
                ? request.getTransactionDate()
                : (dt.getTransactionDate() != null ? dt.getTransactionDate().toLocalDate() : LocalDate.now());

        if (dt.getTransactionType() == DetectedTransactionType.DEBIT) {
            ExpenseCategory category = (request != null && request.getExpenseCategory() != null)
                    ? request.getExpenseCategory()
                    : parseExpenseCategory(dt.getSuggestedCategory());

            String desc = (request != null && request.getDescription() != null && !request.getDescription().isBlank())
                    ? request.getDescription()
                    : (dt.getMerchant() != null ? dt.getMerchant() : "Auto-detected expense");

            String paymentMethod = (request != null && request.getPaymentMethodOrSource() != null)
                    ? request.getPaymentMethodOrSource()
                    : (dt.getSourceApp() != null ? dt.getSourceApp() : dt.getSourceType().name());

            Expense expense = Expense.builder()
                    .user(user)
                    .category(category)
                    .amount(finalAmount)
                    .expenseDate(txnDate)
                    .description(desc)
                    .paymentMethod(paymentMethod)
                    .build();

            Expense savedExpense = expenseRepository.save(expense);
            dt.setConfirmedExpenseId(savedExpense.getId());

            // Sync budget and check alert
            syncBudgetAndCheckAlerts(savedExpense, user);

            // Trigger AI recalculations
            triggerAiAnalysis(user);

        } else if (dt.getTransactionType() == DetectedTransactionType.CREDIT) {
            IncomeCategory category = (request != null && request.getIncomeCategory() != null)
                    ? request.getIncomeCategory()
                    : parseIncomeCategory(dt.getSuggestedCategory());

            String source = (request != null && request.getPaymentMethodOrSource() != null && !request.getPaymentMethodOrSource().isBlank())
                    ? request.getPaymentMethodOrSource()
                    : (dt.getMerchant() != null ? dt.getMerchant() : "Auto-detected income");

            String desc = (request != null && request.getDescription() != null)
                    ? request.getDescription()
                    : ("Auto-detected via " + dt.getSourceType().name());

            Income income = Income.builder()
                    .user(user)
                    .source(source)
                    .category(category)
                    .amount(finalAmount)
                    .incomeDate(txnDate)
                    .description(desc)
                    .isRecurring(false)
                    .build();

            Income savedIncome = incomeRepository.save(income);
            dt.setConfirmedIncomeId(savedIncome.getId());

            // Trigger AI recalculations
            triggerAiAnalysis(user);

        } else if (dt.getTransactionType() == DetectedTransactionType.TRANSFER) {
            log.info("Transfer transaction {} confirmed for user {} (no expense/income created)", dt.getId(), user.getId());
        }

        dt.setStatus(DetectedTransactionStatus.CONFIRMED);
        DetectedTransaction confirmedTxn = detectedTransactionRepository.save(dt);

        log.info("\n" +
                "╔════════════════════════════════════════════════════════════════════════════╗\n" +
                "║ ✅ [FINAI TRANSACTION CONFIRMED & CONVERTED]                              ║\n" +
                "╠════════════════════════════════════════════════════════════════════════════╣\n" +
                "║  🆔 Detected Txn ID : #{}\n" +
                "║  👤 User            : {} ({})\n" +
                "║  💰 Final Amount    : LKR {}\n" +
                "║  📊 Converted To    : {}\n" +
                "║  🏷️ Final Category  : {}\n" +
                "║  🤖 AI Status       : Recalculations Triggered Successfully\n" +
                "╚════════════════════════════════════════════════════════════════════════════╝",
                confirmedTxn.getId(), user.getId(), user.getEmail(),
                finalAmount,
                confirmedTxn.getTransactionType() == DetectedTransactionType.DEBIT ? "EXPENSE (ID #" + confirmedTxn.getConfirmedExpenseId() + ")" :
                        (confirmedTxn.getTransactionType() == DetectedTransactionType.CREDIT ? "INCOME (ID #" + confirmedTxn.getConfirmedIncomeId() + ")" : "TRANSFER REVIEWED"),
                confirmedTxn.getSuggestedCategory() != null ? confirmedTxn.getSuggestedCategory() : "GENERAL");

        return mapToResponse(confirmedTxn);
    }

    @Override
    @Transactional
    public DetectedTransactionResponse ignoreTransaction(Long id, User user) {
        DetectedTransaction dt = detectedTransactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("DetectedTransaction", "id", id));
        dt.setStatus(DetectedTransactionStatus.IGNORED);
        DetectedTransaction ignored = detectedTransactionRepository.save(dt);
        log.info("🚫 [FINAI TRANSACTION IGNORED] Detected Txn ID #{} dismissed by User #{}", id, user.getId());
        return mapToResponse(ignored);
    }

    @Override
    @Transactional
    public DetectionSettingsResponse getSettings(User user) {
        TransactionDetectionSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> settingsRepository.save(
                        TransactionDetectionSettings.builder()
                                .user(user)
                                .smsEnabled(false)
                                .notificationEnabled(false)
                                .confirmationRequired(true)
                                .build()
                ));
        return mapSettingsToResponse(settings);
    }

    @Override
    @Transactional
    public DetectionSettingsResponse updateSettings(DetectionSettingsRequest request, User user) {
        TransactionDetectionSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> TransactionDetectionSettings.builder().user(user).build());

        if (request.getSmsEnabled() != null) settings.setSmsEnabled(request.getSmsEnabled());
        if (request.getNotificationEnabled() != null) settings.setNotificationEnabled(request.getNotificationEnabled());
        if (request.getConfirmationRequired() != null) settings.setConfirmationRequired(request.getConfirmationRequired());

        return mapSettingsToResponse(settingsRepository.save(settings));
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
            log.warn("Error syncing budget on detected expense confirmation: {}", e.getMessage());
        }
    }

    private void triggerAiAnalysis(User user) {
        try {
            aiService.runFullAnalysis(user);
            log.info("AI Analysis triggered successfully after confirming detected transaction for user: {}", user.getId());
        } catch (Exception e) {
            log.warn("Could not immediately refresh AI Analysis for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private ExpenseCategory parseExpenseCategory(String suggested) {
        if (suggested != null && !suggested.isBlank()) {
            try {
                return ExpenseCategory.valueOf(suggested.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return ExpenseCategory.OTHER;
    }

    private IncomeCategory parseIncomeCategory(String suggested) {
        if (suggested != null && !suggested.isBlank()) {
            try {
                return IncomeCategory.valueOf(suggested.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return IncomeCategory.OTHER;
    }

    private DetectedTransactionResponse mapToResponse(DetectedTransaction dt) {
        return DetectedTransactionResponse.builder()
                .id(dt.getId())
                .sourceType(dt.getSourceType())
                .sourceApp(dt.getSourceApp())
                .sourceSender(dt.getSourceSender())
                .amount(dt.getAmount())
                .transactionType(dt.getTransactionType())
                .merchant(dt.getMerchant())
                .accountReference(dt.getAccountReference())
                .transactionDate(dt.getTransactionDate())
                .reference(dt.getReference())
                .rawTextHash(dt.getRawTextHash())
                .confidence(dt.getConfidence())
                .status(dt.getStatus())
                .suggestedCategory(dt.getSuggestedCategory())
                .confirmedIncomeId(dt.getConfirmedIncomeId())
                .confirmedExpenseId(dt.getConfirmedExpenseId())
                .createdAt(dt.getCreatedAt())
                .updatedAt(dt.getUpdatedAt())
                .build();
    }

    private DetectionSettingsResponse mapSettingsToResponse(TransactionDetectionSettings s) {
        return DetectionSettingsResponse.builder()
                .id(s.getId())
                .smsEnabled(s.getSmsEnabled())
                .notificationEnabled(s.getNotificationEnabled())
                .confirmationRequired(s.getConfirmationRequired())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
