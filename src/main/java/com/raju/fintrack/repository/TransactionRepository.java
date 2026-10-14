package com.raju.fintrack.repository;

import com.raju.fintrack.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDescIdDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDescIdDesc(User user, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    boolean existsByAccount(Account account);

    boolean existsByCategory(Category category);

    boolean existsByUser(User user);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t " +
           "where t.user = :user and t.type = :type and t.date between :from and :to")
    BigDecimal sumByType(@Param("user") User user, @Param("type") TransactionType type,
                         @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t " +
           "where t.user = :user and t.category = :category and t.date between :from and :to")
    BigDecimal sumByCategory(@Param("user") User user, @Param("category") Category category,
                             @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Rows: [year, month, type, sum] */
    @Query("select year(t.date), month(t.date), t.type, sum(t.amount) from Transaction t " +
           "where t.user = :user and t.date between :from and :to " +
           "group by year(t.date), month(t.date), t.type")
    List<Object[]> monthlyTotals(@Param("user") User user, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Rows: [categoryName, sum] */
    @Query("select c.name, sum(t.amount) from Transaction t join t.category c " +
           "where t.user = :user and t.type = :type and t.date between :from and :to " +
           "group by c.name order by sum(t.amount) desc")
    List<Object[]> totalsByCategory(@Param("user") User user, @Param("type") TransactionType type,
                                    @Param("from") LocalDate from, @Param("to") LocalDate to);
}
