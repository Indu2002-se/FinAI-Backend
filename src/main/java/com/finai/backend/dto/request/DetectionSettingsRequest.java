package com.finai.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionSettingsRequest {
    private Boolean smsEnabled;
    private Boolean notificationEnabled;
    private Boolean confirmationRequired;
}
