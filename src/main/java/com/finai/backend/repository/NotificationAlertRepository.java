package com.finai.backend.repository;

import com.finai.backend.entity.NotificationAlert;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationAlertRepository extends JpaRepository<NotificationAlert, Long> {
    List<NotificationAlert> findByUserOrderByCreatedAtDesc(User user);
    List<NotificationAlert> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}
