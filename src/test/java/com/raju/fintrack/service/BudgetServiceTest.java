package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.BudgetStatusDto;
import com.raju.fintrack.entity.Budget;
import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.repository.BudgetRepository;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepo;
    @Mock CategoryRepository categoryRepo;
    @Mock TransactionRepository txRepo;
    @InjectMocks BudgetService service;

    private Budget budget(Category cat, String limit) {
        Budget b = new Budget();
        b.setId(1L);
        b.setMonth("2026-10");
        b.setCategory(cat);
        b.setLimitAmount(new BigDecimal(limit));
        return b;
    }

    private Category food() {
        Category c = new Category();
        c.setId(7L);
        c.setName("Food & Groceries");
        return c;
    }

    @Test
    void flagsOverBudgetWhenSpentExceedsLimit() {
        User user = new User();
        Category cat = food();
        when(budgetRepo.findByUserAndMonth(user, "2026-10")).thenReturn(List.of(budget(cat, "500")));
        when(txRepo.sumByCategory(eq(user), eq(cat), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("600"));

        BudgetStatusDto s = service.status(user, "2026-10").get(0);

        assertTrue(s.overBudget());
        assertFalse(s.warning());
        assertEquals(120, s.percentUsed());
        assertEquals(new BigDecimal("-100"), s.remaining());
    }

    @Test
    void warnsAtEightyPercent() {
        User user = new User();
        Category cat = food();
        when(budgetRepo.findByUserAndMonth(user, "2026-10")).thenReturn(List.of(budget(cat, "1000")));
        when(txRepo.sumByCategory(eq(user), eq(cat), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("850"));

        BudgetStatusDto s = service.status(user, "2026-10").get(0);

        assertFalse(s.overBudget());
        assertTrue(s.warning());
        assertEquals(85, s.percentUsed());
    }
}
