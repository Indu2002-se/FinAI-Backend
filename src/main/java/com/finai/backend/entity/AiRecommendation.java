package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "top_driver", length = 100)
    private String topDriver;

    @Column(name = "recommendation_text", nullable = false, columnDefinition = "TEXT")
    private String recommendationText;

    @Column(name = "action_items_json", columnDefinition = "TEXT")
    private String actionItemsJson;

    @Column(name = "is_applied")
    @Builder.Default
    private Boolean isApplied = false;
}
