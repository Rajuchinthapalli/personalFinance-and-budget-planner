package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.TransactionDto;
import com.raju.fintrack.dto.Dtos.TransactionRequest;
import com.raju.fintrack.entity.*;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.AccountRepository;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.TransactionRepository;
import com.raju.fintrack.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    @Transactional(readOnly = true)
    public List<TransactionDto> list(User user, String month) {
        List<Transaction> rows;
        if (month == null || month.isBlank()) {
            rows = txRepo.findByUserOrderByDateDescIdDesc(user);
        } else {
            LocalDate[] r = DateUtil.range(month);
            rows = txRepo.findByUserAndDateBetweenOrderByDateDescIdDesc(user, r[0], r[1]);
        }
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional
    public TransactionDto create(User user, TransactionRequest req) {
        Transaction t = new Transaction();
        t.setUser(user);
        fill(t, user, req);
        applyToBalance(t.getAccount(), t.getType(), t.getAmount(), 1);
        return toDto(txRepo.save(t));
    }

    @Transactional
    public TransactionDto update(User user, Long id, TransactionRequest req) {
        Transaction t = find(user, id);
        applyToBalance(t.getAccount(), t.getType(), t.getAmount(), -1);   // undo old effect
        fill(t, user, req);
        applyToBalance(t.getAccount(), t.getType(), t.getAmount(), 1);    // apply new effect
        return toDto(txRepo.save(t));
    }

    @Transactional
    public void delete(User user, Long id) {
        Transaction t = find(user, id);
        applyToBalance(t.getAccount(), t.getType(), t.getAmount(), -1);
        txRepo.delete(t);
    }

    private Transaction find(User user, Long id) {
        return txRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private void fill(Transaction t, User user, TransactionRequest req) {
        Account account = accountRepo.findByIdAndUser(req.accountId(), user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
        Category category = categoryRepo.findByIdAndUser(req.categoryId(), user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        t.setAccount(account);
        t.setCategory(category);
        t.setType(category.getType());      // income/expense is decided by the category
        t.setAmount(req.amount());
        t.setDate(req.date());
        t.setNote(req.note());
    }

    /** sign = +1 applies the transaction to the balance, -1 reverses it. */
    private void applyToBalance(Account account, TransactionType type, BigDecimal amount, int sign) {
        BigDecimal delta = type == TransactionType.INCOME ? amount : amount.negate();
        if (sign < 0) delta = delta.negate();
        account.setBalance(account.getBalance().add(delta));
        accountRepo.save(account);
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getAmount(), t.getDate(), t.getNote(), t.getType(),
                t.getAccount().getId(), t.getAccount().getName(),
                t.getCategory().getId(), t.getCategory().getName());
    }
}
