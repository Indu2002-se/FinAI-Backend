package com.finai.backend.service.impl;

import com.finai.backend.dto.request.IncomeRequest;
import com.finai.backend.dto.response.IncomeResponse;
import com.finai.backend.entity.Income;
import com.finai.backend.entity.User;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.IncomeRepository;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.service.interfaces.IncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final AiService aiService;

    @Override
    @Transactional(readOnly = true)
    public List<IncomeResponse> getAllIncomes(User user) {
        return incomeRepository.findByUserOrderByIncomeDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(Long id, User user) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", id));
        return mapToResponse(income);
    }

    @Override
    @Transactional
    public IncomeResponse createIncome(IncomeRequest request, User user) {
        Income income = Income.builder()
                .user(user)
                .source(request.getSource())
                .category(request.getCategory())
                .amount(request.getAmount())
                .incomeDate(request.getIncomeDate())
                .description(request.getDescription())
                .isRecurring(request.getIsRecurring() != null && request.getIsRecurring())
                .build();

        Income saved = incomeRepository.save(income);
        triggerAiAnalysis(user);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request, User user) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", id));

        income.setSource(request.getSource());
        income.setCategory(request.getCategory());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setDescription(request.getDescription());
        if (request.getIsRecurring() != null) {
            income.setIsRecurring(request.getIsRecurring());
        }

        Income updated = incomeRepository.save(income);
        triggerAiAnalysis(user);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id, User user) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", id));
        incomeRepository.delete(income);
        triggerAiAnalysis(user);
    }

    private void triggerAiAnalysis(User user) {
        try {
            aiService.runFullAnalysis(user);
        } catch (Exception e) {
            log.warn("AI analysis trigger failed after income change: {}", e.getMessage());
        }
    }

    public IncomeResponse mapToResponse(Income income) {
        return IncomeResponse.builder()
                .id(income.getId())
                .source(income.getSource())
                .category(income.getCategory())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate())
                .description(income.getDescription())
                .isRecurring(income.getIsRecurring())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }
}
