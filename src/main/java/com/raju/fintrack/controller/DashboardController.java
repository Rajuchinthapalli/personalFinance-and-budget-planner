package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.CategoryTotal;
import com.raju.fintrack.dto.Dtos.MonthlyDto;
import com.raju.fintrack.dto.Dtos.SummaryDto;
import com.raju.fintrack.entity.TransactionType;
import com.raju.fintrack.security.CurrentUser;
import com.raju.fintrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;
    private final CurrentUser currentUser;

    @GetMapping("/summary")
    public SummaryDto summary(@RequestParam(required = false) String month) {
        return service.summary(currentUser.get(), month);
    }

    @GetMapping("/monthly")
    public List<MonthlyDto> monthly() {
        return service.monthly(currentUser.get());
    }

    @GetMapping("/by-category")
    public List<CategoryTotal> byCategory(@RequestParam(required = false) String month,
                                          @RequestParam(defaultValue = "EXPENSE") TransactionType type) {
        return service.byCategory(currentUser.get(), month, type);
    }
}
