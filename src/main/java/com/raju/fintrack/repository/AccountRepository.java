package com.raju.fintrack.repository;

import com.raju.fintrack.entity.Account;
import com.raju.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserOrderByIdAsc(User user);
    Optional<Account> findByIdAndUser(Long id, User user);

    @Query("select coalesce(sum(a.balance), 0) from Account a where a.user = :user")
    BigDecimal totalBalance(@Param("user") User user);
}
