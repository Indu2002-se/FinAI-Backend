package com.finai.backend.repository;

import com.finai.backend.entity.Budget;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndBudgetMonth(User user, String budgetMonth);
    Optional<Budget> findByUserAndCategoryAndBudgetMonth(User user, ExpenseCategory category, String budgetMonth);
}
