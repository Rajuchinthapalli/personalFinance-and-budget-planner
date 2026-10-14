package com.raju.fintrack.repository;

import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByTypeAscNameAsc(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
}
