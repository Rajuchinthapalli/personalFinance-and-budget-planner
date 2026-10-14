package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.TransactionDto;
import com.raju.fintrack.dto.Dtos.TransactionRequest;
import com.raju.fintrack.security.CurrentUser;
import com.raju.fintrack.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;
    private final CurrentUser currentUser;

    /** Optional ?month=yyyy-MM filter. */
    @GetMapping
    public List<TransactionDto> list(@RequestParam(required = false) String month) {
        return service.list(currentUser.get(), month);
    }

    @PostMapping
    public TransactionDto create(@Valid @RequestBody TransactionRequest req) {
        return service.create(currentUser.get(), req);
    }

    @PutMapping("/{id}")
    public TransactionDto update(@PathVariable Long id, @Valid @RequestBody TransactionRequest req) {
        return service.update(currentUser.get(), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }
}
