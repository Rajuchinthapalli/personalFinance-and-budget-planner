package com.raju.fintrack.repository;

import com.raju.fintrack.entity.Budget;
import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndMonth(User user, String month);
    Optional<Budget> findByUserAndCategoryAndMonth(User user, Category category, String month);
    Optional<Budget> findByIdAndUser(Long id, User user);
    void deleteByCategory(Category category);
}
