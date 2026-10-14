package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.AccountRequest;
import com.raju.fintrack.entity.Account;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.AccountRepository;
import com.raju.fintrack.repository.TransactionRepository;
import com.raju.fintrack.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final CurrentUser currentUser;

    @GetMapping
    public List<Account> list() {
        return accountRepo.findByUserOrderByIdAsc(currentUser.get());
    }

    @PostMapping
    public Account create(@Valid @RequestBody AccountRequest req) {
        Account a = new Account();
        a.setUser(currentUser.get());
        return save(a, req);
    }

    @PutMapping("/{id}")
    public Account update(@PathVariable Long id, @Valid @RequestBody AccountRequest req) {
        return save(find(id), req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Account a = find(id);
        if (txRepo.existsByAccount(a)) {
            throw new ApiException(HttpStatus.CONFLICT, "This account has transactions. Delete them first.");
        }
        accountRepo.delete(a);
    }

    private Account find(Long id) {
        User user = currentUser.get();
        return accountRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private Account save(Account a, AccountRequest req) {
        a.setName(req.name().trim());
        a.setType(req.type());
        a.setBalance(req.balance());
        return accountRepo.save(a);
    }
}
