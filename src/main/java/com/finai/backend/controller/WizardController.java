package com.finai.backend.controller;

import com.finai.backend.dto.request.WizardRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.WizardResponse;
import com.finai.backend.service.interfaces.WizardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Wizard controller
 * Handles user onboarding wizard endpoints
 */
@RestController
@RequestMapping("/api/v1/wizard")
@RequiredArgsConstructor
@Tag(name = "Wizard", description = "User onboarding wizard management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WizardController {

    private final WizardService wizardService;

    @PostMapping
    @Operation(
            summary = "Save wizard profile",
            description = "Save onboarding wizard profile for authenticated user"
    )
    public ResponseEntity<ApiResponse<WizardResponse>> saveWizard(
            @Valid @RequestBody WizardRequest request) {
        WizardResponse response = wizardService.saveWizard(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wizard profile created successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Get wizard profile",
            description = "Get wizard profile for authenticated user"
    )
    public ResponseEntity<ApiResponse<WizardResponse>> getWizard() {
        WizardResponse response = wizardService.getWizard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(
            summary = "Update wizard profile",
            description = "Update wizard profile for authenticated user"
    )
    public ResponseEntity<ApiResponse<WizardResponse>> updateWizard(
            @Valid @RequestBody WizardRequest request) {
        WizardResponse response = wizardService.updateWizard(request);
        return ResponseEntity.ok(ApiResponse.success("Wizard profile updated successfully", response));
    }
}
