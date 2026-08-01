package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.WizardRequest;
import com.finai.backend.dto.response.WizardResponse;

/**
 * Wizard service interface
 * Defines wizard profile operations
 */
public interface WizardService {

    /**
     * Save wizard profile for authenticated user
     * @param request wizard request
     * @return wizard response
     */
    WizardResponse saveWizard(WizardRequest request);

    /**
     * Get wizard profile for authenticated user
     * @return wizard response
     */
    WizardResponse getWizard();

    /**
     * Update wizard profile for authenticated user
     * @param request wizard request
     * @return wizard response
     */
    WizardResponse updateWizard(WizardRequest request);
}
