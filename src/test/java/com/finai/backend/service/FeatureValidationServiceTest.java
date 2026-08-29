package com.finai.backend.service;

import com.finai.backend.dto.response.FeatureValidationResult;
import com.finai.backend.service.impl.FeatureValidationServiceImpl;
import com.finai.backend.service.interfaces.FeatureValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeatureValidationServiceTest {

    private FeatureValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new FeatureValidationServiceImpl();
    }

    private Map<String, Object> createValid42FeatureMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String col : FeatureValidationService.EXPECTED_FEATURE_NAMES) {
            map.put(col, 0.0);
        }
        return map;
    }

    @Test
    @DisplayName("Valid 42-feature ordered vector passes validation")
    void testValid42FeatureVectorPasses() {
        Map<String, Object> features = createValid42FeatureMap();
        FeatureValidationResult result = validationService.validate(features);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Vector with 40 features fails with length mismatch error")
    void testVectorWith40FeaturesFailsLengthMismatch() {
        Map<String, Object> features = createValid42FeatureMap();
        features.remove("instalment_goods_flag");
        features.remove("instalment_amount");

        FeatureValidationResult result = validationService.validate(features);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("length mismatch")));
    }

    @Test
    @DisplayName("Vector with null value fails validation")
    void testVectorWithNullValueFails() {
        Map<String, Object> features = createValid42FeatureMap();
        features.put("total_income", null);

        FeatureValidationResult result = validationService.validate(features);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("null value") && e.contains("total_income")));
    }

    @Test
    @DisplayName("Vector with mismatched feature order fails validation")
    void testVectorWithMismatchedOrderFails() {
        Map<String, Object> features = new LinkedHashMap<>();
        // Swap first two columns
        features.put("gender", 1.0);
        features.put("age", 30.0);
        for (int i = 2; i < FeatureValidationService.EXPECTED_FEATURE_NAMES.size(); i++) {
            features.put(FeatureValidationService.EXPECTED_FEATURE_NAMES.get(i), 0.0);
        }

        FeatureValidationResult result = validationService.validate(features);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("order mismatch")));
    }

    @Test
    @DisplayName("Null feature vector fails validation")
    void testNullFeatureVectorFails() {
        FeatureValidationResult result = validationService.validate(null);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Feature vector is null"));
    }
}
