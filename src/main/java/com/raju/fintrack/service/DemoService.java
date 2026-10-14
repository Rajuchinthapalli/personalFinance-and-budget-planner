package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.TransactionRequest;
import com.raju.fintrack.entity.*;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.AccountRepository;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

/** Fills an empty account with 12 months of sample data so the dashboard charts look alive. */
@Service
@RequiredArgsConstructor
public class DemoService {
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final TransactionRepository txRepo;
    private final TransactionService txService;

    @Transactional
    public void seed(User user) {
        if (txRepo.existsByUser(user)) {
            throw new ApiException(HttpStatus.CONFLICT, "Demo data can only be loaded into an empty account");
        }
        List<Account> accounts = accountRepo.findByUserOrderByIdAsc(user);
        if (accounts.isEmpty()) {
            accounts = List.of(newAccount(user, "Main Account", AccountType.CHECKING),
                               newAccount(user, "Savings", AccountType.SAVINGS));
        }
        List<Category> categories = categoryRepo.findByUserOrderByTypeAscNameAsc(user);
        List<Category> expenses = categories.stream().filter(c -> c.getType() == TransactionType.EXPENSE).toList();
        Category salary = categories.stream()
                .filter(c -> c.getType() == TransactionType.INCOME && c.getName().equals("Salary"))
                .findFirst()
                .orElseGet(() -> categories.stream().filter(c -> c.getType() == TransactionType.INCOME).findFirst()
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Add an income category first")));
        if (expenses.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Add an expense category first");

        Random rnd = new Random();
        Account main = accounts.get(0);
        LocalDate today = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            int maxDay = i == 0 ? today.getDayOfMonth() : ym.lengthOfMonth();

            BigDecimal pay = BigDecimal.valueOf(45000 + rnd.nextInt(8000));
            txService.create(user, new TransactionRequest(pay, ym.atDay(1), "Monthly salary", main.getId(), salary.getId()));

            int count = 8 + rnd.nextInt(6);
            for (int k = 0; k < count; k++) {
                Category c = expenses.get(rnd.nextInt(expenses.size()));
                BigDecimal amount = BigDecimal.valueOf(150 + rnd.nextInt(4500));
                LocalDate day = ym.atDay(1 + rnd.nextInt(maxDay));
                txService.create(user, new TransactionRequest(amount, day, "Sample " + c.getName(), main.getId(), c.getId()));
            }
        }
    }

    private Account newAccount(User user, String name, AccountType type) {
        Account a = new Account();
        a.setName(name);
        a.setType(type);
        a.setBalance(BigDecimal.ZERO);
        a.setUser(user);
        return accountRepo.save(a);
    }
}
