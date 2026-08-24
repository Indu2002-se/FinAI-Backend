package com.finai.backend.entity;

import com.finai.backend.entity.enums.RewardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "badge_icon", length = 100)
    private String badgeIcon;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    @Column(name = "points_awarded", nullable = false)
    @Builder.Default
    private Integer pointsAwarded = 0;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;
}
