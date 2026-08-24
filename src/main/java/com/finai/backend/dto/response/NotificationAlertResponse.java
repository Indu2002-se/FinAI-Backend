package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.AlertType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationAlertResponse {
    private Long id;
    private String title;
    private String message;
    private AlertType alertType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
