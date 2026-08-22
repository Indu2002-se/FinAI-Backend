package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_detection_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetectionSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private Boolean smsEnabled = false;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = false;

    @Column(name = "confirmation_required", nullable = false)
    @Builder.Default
    private Boolean confirmationRequired = true;
}
