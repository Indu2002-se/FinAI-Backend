package com.finai.backend.repository;

import com.finai.backend.entity.ExpenseForecast;
import com.finai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseForecastRepository extends JpaRepository<ExpenseForecast, Long> {
    List<ExpenseForecast> findByUserOrderByForecastDateAsc(User user);
    void deleteByUser(User user);
}
