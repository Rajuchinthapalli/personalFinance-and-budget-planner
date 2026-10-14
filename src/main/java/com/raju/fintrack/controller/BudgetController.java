package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.BudgetRequest;
import com.raju.fintrack.dto.Dtos.BudgetStatusDto;
import com.raju.fintrack.security.CurrentUser;
import com.raju.fintrack.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService service;
    private final CurrentUser currentUser;

    @GetMapping("/status")
    public List<BudgetStatusDto> status(@RequestParam(required = false) String month) {
        return service.status(currentUser.get(), month);
    }

    /** Create, or update the limit if the category already has a budget that month. */
    @PostMapping
    public BudgetStatusDto save(@Valid @RequestBody BudgetRequest req) {
        return service.save(currentUser.get(), req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }
}
