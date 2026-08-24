package com.finai.backend.service.impl;

import com.finai.backend.dto.request.DebtRequest;
import com.finai.backend.dto.response.DebtResponse;
import com.finai.backend.entity.Debt;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.DebtStatus;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.DebtRepository;
import com.finai.backend.service.interfaces.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DebtResponse> getAllDebts(User user) {
        return debtRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id, User user) {
        Debt debt = debtRepository.findById(id)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Debt", "id", id));
        return mapToResponse(debt);
    }

    @Override
    @Transactional
    public DebtResponse createDebt(DebtRequest request, User user) {
        DebtStatus status = request.getStatus() != null ? request.getStatus() : DebtStatus.ACTIVE;
        if (request.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            status = DebtStatus.PAID_OFF;
        }

        Debt debt = Debt.builder()
                .user(user)
                .debtName(request.getDebtName())
                .source(request.getSource())
                .totalAmount(request.getTotalAmount())
                .remainingAmount(request.getRemainingAmount())
                .monthlyPayment(request.getMonthlyPayment())
                .interestRate(request.getInterestRate())
                .dueDate(request.getDueDate())
                .status(status)
                .isCreditCard(request.getIsCreditCard() != null && request.getIsCreditCard())
                .build();

        return mapToResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public DebtResponse updateDebt(Long id, DebtRequest request, User user) {
        Debt debt = debtRepository.findById(id)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Debt", "id", id));

        debt.setDebtName(request.getDebtName());
        debt.setSource(request.getSource());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setRemainingAmount(request.getRemainingAmount());
        debt.setMonthlyPayment(request.getMonthlyPayment());
        debt.setInterestRate(request.getInterestRate());
        debt.setDueDate(request.getDueDate());

        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID_OFF);
        } else if (request.getStatus() != null) {
            debt.setStatus(request.getStatus());
        }

        if (request.getIsCreditCard() != null) {
            debt.setIsCreditCard(request.getIsCreditCard());
        }

        return mapToResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public void deleteDebt(Long id, User user) {
        Debt debt = debtRepository.findById(id)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Debt", "id", id));
        debtRepository.delete(debt);
    }

    public DebtResponse mapToResponse(Debt debt) {
        return DebtResponse.builder()
                .id(debt.getId())
                .debtName(debt.getDebtName())
                .source(debt.getSource())
                .totalAmount(debt.getTotalAmount())
                .remainingAmount(debt.getRemainingAmount())
                .monthlyPayment(debt.getMonthlyPayment())
                .interestRate(debt.getInterestRate())
                .dueDate(debt.getDueDate())
                .status(debt.getStatus())
                .isCreditCard(debt.getIsCreditCard())
                .createdAt(debt.getCreatedAt())
                .updatedAt(debt.getUpdatedAt())
                .build();
    }
}
