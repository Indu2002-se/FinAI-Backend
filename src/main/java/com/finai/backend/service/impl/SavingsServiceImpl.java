package com.finai.backend.service.impl;

import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.request.SavingsRequest;
import com.finai.backend.dto.response.SavingsGoalResponse;
import com.finai.backend.dto.response.SavingsResponse;
import com.finai.backend.entity.ChildProfile;
import com.finai.backend.entity.Savings;
import com.finai.backend.entity.SavingsGoal;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.GoalStatus;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.ChildProfileRepository;
import com.finai.backend.repository.SavingsGoalRepository;
import com.finai.backend.repository.SavingsRepository;
import com.finai.backend.service.interfaces.SavingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsServiceImpl implements SavingsService {

    private final SavingsRepository savingsRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final ChildProfileRepository childProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SavingsResponse> getAllSavings(User user) {
        return savingsRepository.findByUser(user)
                .stream()
                .map(this::mapToSavingsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsResponse getSavingsById(Long id, User user) {
        Savings s = savingsRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Savings", "id", id));
        return mapToSavingsResponse(s);
    }

    @Override
    @Transactional
    public SavingsResponse createSavings(SavingsRequest request, User user) {
        Savings savings = Savings.builder()
                .user(user)
                .accountName(request.getAccountName())
                .bankName(request.getBankName())
                .currentBalance(request.getCurrentBalance())
                .targetMonthlyDeposit(request.getTargetMonthlyDeposit())
                .notes(request.getNotes())
                .build();
        return mapToSavingsResponse(savingsRepository.save(savings));
    }

    @Override
    @Transactional
    public SavingsResponse updateSavings(Long id, SavingsRequest request, User user) {
        Savings savings = savingsRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Savings", "id", id));

        savings.setAccountName(request.getAccountName());
        savings.setBankName(request.getBankName());
        savings.setCurrentBalance(request.getCurrentBalance());
        savings.setTargetMonthlyDeposit(request.getTargetMonthlyDeposit());
        savings.setNotes(request.getNotes());

        return mapToSavingsResponse(savingsRepository.save(savings));
    }

    @Override
    @Transactional
    public void deleteSavings(Long id, User user) {
        Savings savings = savingsRepository.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Savings", "id", id));
        savingsRepository.delete(savings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getAllGoals(User user) {
        return savingsGoalRepository.findByUser(user)
                .stream()
                .map(this::mapToGoalResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoalById(Long id, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        return mapToGoalResponse(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request, User user) {
        ChildProfile childProfile = null;
        if (request.getChildProfileId() != null) {
            childProfile = childProfileRepository.findById(request.getChildProfileId())
                    .filter(c -> c.getParentUser().getId().equals(user.getId()))
                    .orElse(null);
        }

        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .childProfile(childProfile)
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .deadline(request.getDeadline())
                .status(request.getStatus() != null ? request.getStatus() : GoalStatus.IN_PROGRESS)
                .category(request.getCategory())
                .icon(request.getIcon())
                .notes(request.getNotes())
                .build();

        return mapToGoalResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));

        goal.setTitle(request.getTitle());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
                goal.setStatus(GoalStatus.COMPLETED);
            }
        }
        goal.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        goal.setCategory(request.getCategory());
        goal.setIcon(request.getIcon());
        goal.setNotes(request.getNotes());

        return mapToGoalResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long id, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        savingsGoalRepository.delete(goal);
    }

    public SavingsResponse mapToSavingsResponse(Savings s) {
        return SavingsResponse.builder()
                .id(s.getId())
                .accountName(s.getAccountName())
                .bankName(s.getBankName())
                .currentBalance(s.getCurrentBalance())
                .targetMonthlyDeposit(s.getTargetMonthlyDeposit())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    public SavingsGoalResponse mapToGoalResponse(SavingsGoal g) {
        double progress = 0.0;
        if (g.getTargetAmount() != null && g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            progress = Math.min(100.0, Math.max(0.0, progress));
        }

        return SavingsGoalResponse.builder()
                .id(g.getId())
                .childProfileId(g.getChildProfile() != null ? g.getChildProfile().getId() : null)
                .childName(g.getChildProfile() != null ? g.getChildProfile().getFirstName() : null)
                .title(g.getTitle())
                .targetAmount(g.getTargetAmount())
                .currentAmount(g.getCurrentAmount())
                .progressPercentage(progress)
                .deadline(g.getDeadline())
                .status(g.getStatus())
                .category(g.getCategory())
                .icon(g.getIcon())
                .notes(g.getNotes())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
