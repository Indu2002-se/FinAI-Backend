package com.finai.backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendationResponse {
    private String category;
    private String topDriver;
    private String recommendationText;
    private List<String> actionItems;
    private Boolean isApplied;
}
