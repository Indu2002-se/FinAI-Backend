package com.finai.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionSettingsResponse {
    private Long id;
    private Boolean smsEnabled;
    private Boolean notificationEnabled;
    private Boolean confirmationRequired;
    private LocalDateTime updatedAt;
}
