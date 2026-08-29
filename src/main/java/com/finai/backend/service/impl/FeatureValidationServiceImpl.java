package com.finai.backend.service.impl;

import com.finai.backend.dto.response.FeatureValidationResult;
import com.finai.backend.service.interfaces.FeatureValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FeatureValidationServiceImpl implements FeatureValidationService {

    @Override
    public FeatureValidationResult validate(Map<String, Object> featureVector) {
        List<String> errors = new ArrayList<>();

        if (featureVector == null) {
            errors.add("Feature vector is null");
            return FeatureValidationResult.failure(errors);
        }

        if (featureVector.size() != EXPECTED_FEATURE_NAMES.size()) {
            errors.add(String.format("Feature vector length mismatch: expected %d, got %d",
                    EXPECTED_FEATURE_NAMES.size(), featureVector.size()));
        }

        // Validate presence and non-null values
        for (String expectedCol : EXPECTED_FEATURE_NAMES) {
            if (!featureVector.containsKey(expectedCol)) {
                errors.add("Missing required feature: " + expectedCol);
            } else if (featureVector.get(expectedCol) == null) {
                errors.add("Feature has null value: " + expectedCol);
            }
        }

        // Validate order if ordered map
        Iterator<String> keyIter = featureVector.keySet().iterator();
        int index = 0;
        while (keyIter.hasNext() && index < EXPECTED_FEATURE_NAMES.size()) {
            String actualKey = keyIter.next();
            String expectedKey = EXPECTED_FEATURE_NAMES.get(index);
            if (!expectedKey.equals(actualKey)) {
                errors.add(String.format("Feature order mismatch at index %d: expected '%s', got '%s'",
                        index, expectedKey, actualKey));
                break; // Report first order mismatch
            }
            index++;
        }

        if (!errors.isEmpty()) {
            log.warn("Feature validation failed with {} errors: {}", errors.size(), errors);
            return FeatureValidationResult.failure(errors);
        }

        log.debug("Feature validation passed successfully with 42 features.");
        return FeatureValidationResult.success();
    }
}
