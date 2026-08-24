package com.finai.backend.service.interfaces;

import com.finai.backend.dto.response.DashboardResponse;
import com.finai.backend.entity.User;

public interface DashboardService {
    DashboardResponse getDashboardData(User user);
}
