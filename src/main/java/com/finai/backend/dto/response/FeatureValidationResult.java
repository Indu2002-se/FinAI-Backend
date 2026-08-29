package com.finai.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureValidationResult {
    private boolean valid;
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public static FeatureValidationResult success() {
        return FeatureValidationResult.builder()
                .valid(true)
                .errors(new ArrayList<>())
                .build();
    }

    public static FeatureValidationResult failure(List<String> errors) {
        return FeatureValidationResult.builder()
                .valid(false)
                .errors(errors != null ? errors : new ArrayList<>())
                .build();
    }
}
