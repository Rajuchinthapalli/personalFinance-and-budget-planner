package com.raju.fintrack.dto;

import com.raju.fintrack.entity.AccountType;
import com.raju.fintrack.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Dtos {
    private Dtos() {}

    // ----- auth
    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String name, String email) {}

    // ----- requests
    public record AccountRequest(@NotBlank String name, @NotNull AccountType type, @NotNull BigDecimal balance) {}
    public record CategoryRequest(@NotBlank String name, @NotNull TransactionType type, Long parentId) {}
    public record TransactionRequest(@NotNull @Positive BigDecimal amount, @NotNull LocalDate date, String note,
                                     @NotNull Long accountId, @NotNull Long categoryId) {}
    public record BudgetRequest(@NotNull Long categoryId,
                                @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}", message = "must be yyyy-MM") String month,
                                @NotNull @Positive BigDecimal limitAmount) {}
    public record GoalRequest(@NotBlank String name, @NotNull @Positive BigDecimal target,
                              BigDecimal saved, LocalDate deadline) {}
    public record ContributeRequest(@NotNull @Positive BigDecimal amount) {}

    // ----- responses
    public record TransactionDto(Long id, BigDecimal amount, LocalDate date, String note, TransactionType type,
                                 Long accountId, String accountName, Long categoryId, String categoryName) {}
    public record SummaryDto(String month, BigDecimal income, BigDecimal expense, BigDecimal net,
                             BigDecimal totalBalance, BigDecimal avgMonthlyExpense) {}
    public record MonthlyDto(String month, BigDecimal income, BigDecimal expense) {}
    public record CategoryTotal(String name, BigDecimal total) {}
    public record BudgetStatusDto(Long id, Long categoryId, String categoryName, String month,
                                  BigDecimal limit, BigDecimal spent, BigDecimal remaining,
                                  int percentUsed, boolean overBudget, boolean warning) {}
}
