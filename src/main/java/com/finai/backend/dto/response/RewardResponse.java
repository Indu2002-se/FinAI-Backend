package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.RewardType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardResponse {
    private Long id;
    private String title;
    private String description;
    private String badgeIcon;
    private RewardType rewardType;
    private Integer pointsAwarded;
    private LocalDateTime unlockedAt;
}
