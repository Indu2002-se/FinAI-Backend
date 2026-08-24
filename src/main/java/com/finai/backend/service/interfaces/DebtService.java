package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.DebtRequest;
import com.finai.backend.dto.response.DebtResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface DebtService {
    List<DebtResponse> getAllDebts(User user);
    DebtResponse getDebtById(Long id, User user);
    DebtResponse createDebt(DebtRequest request, User user);
    DebtResponse updateDebt(Long id, DebtRequest request, User user);
    void deleteDebt(Long id, User user);
}
