package com.finai.backend.service.interfaces;

import com.finai.backend.dto.response.MonthlyReportResponse;
import com.finai.backend.entity.User;

public interface ReportService {
    MonthlyReportResponse getMonthlyReport(String yearMonth, User user);
}
