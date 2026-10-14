package com.raju.fintrack.repository;

import com.raju.fintrack.entity.SavingsGoal;
import com.raju.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUserOrderByIdAsc(User user);
    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
