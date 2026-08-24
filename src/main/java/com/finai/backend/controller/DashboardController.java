package com.finai.backend.controller;

import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.DashboardResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.DashboardService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Main financial dashboard aggregation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get aggregated financial dashboard overview")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        User user = securityUtils.getCurrentUser();
        DashboardResponse response = dashboardService.getDashboardData(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
