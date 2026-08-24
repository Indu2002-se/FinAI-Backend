package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.IncomeRequest;
import com.finai.backend.dto.response.IncomeResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface IncomeService {
    List<IncomeResponse> getAllIncomes(User user);
    IncomeResponse getIncomeById(Long id, User user);
    IncomeResponse createIncome(IncomeRequest request, User user);
    IncomeResponse updateIncome(Long id, IncomeRequest request, User user);
    void deleteIncome(Long id, User user);
}
