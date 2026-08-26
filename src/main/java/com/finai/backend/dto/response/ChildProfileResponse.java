package com.finai.backend.dto.response;

import com.finai.backend.entity.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String avatar;
    private BigDecimal currentSavings;
    private Integer totalPoints;
    private Integer activeGoalsCount;
    private Integer completedQuizzesCount;
    private Integer totalRewardsCount;
    private ChildUserInfo childUser;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildUserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean enabled;
        private Boolean emailVerified;
        private Boolean profileComplete;
    }
}
