package com.finai.backend.controller;

import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.MonthlyReportResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.ReportService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Comprehensive monthly financial report endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly financial report for current or specified month")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @RequestParam(required = false) String month) {
        User user = securityUtils.getCurrentUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(month, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "Get monthly financial report by year and month")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReportByYearAndMonth(
            @PathVariable String year,
            @PathVariable String month) {
        User user = securityUtils.getCurrentUser();
        String formattedMonth = String.format("%s-%02d", year, Integer.parseInt(month));
        MonthlyReportResponse response = reportService.getMonthlyReport(formattedMonth, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
