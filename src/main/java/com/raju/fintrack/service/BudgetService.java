package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.BudgetRequest;
import com.raju.fintrack.dto.Dtos.BudgetStatusDto;
import com.raju.fintrack.entity.Budget;
import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.TransactionType;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.BudgetRepository;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.TransactionRepository;
import com.raju.fintrack.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepo;
    private final CategoryRepository categoryRepo;
    private final TransactionRepository txRepo;

    /** Spent vs limit for every budget in the month, with over-budget / 80% warning flags. */
    @Transactional(readOnly = true)
    public List<BudgetStatusDto> status(User user, String month) {
        String m = DateUtil.parse(month).toString();
        LocalDate[] r = DateUtil.range(m);
        return budgetRepo.findByUserAndMonth(user, m).stream()
                .map(b -> toStatus(user, b, r))
                .toList();
    }

    /** Creates a budget, or updates the limit if one already exists for that category and month. */
    @Transactional
    public BudgetStatusDto save(User user, BudgetRequest req) {
        Category category = categoryRepo.findByIdAndUser(req.categoryId(), user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        if (category.getType() != TransactionType.EXPENSE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Budgets can only be set for expense categories");
        }
        Budget budget = budgetRepo.findByUserAndCategoryAndMonth(user, category, req.month()).orElseGet(Budget::new);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonth(req.month());
        budget.setLimitAmount(req.limitAmount());
        budgetRepo.save(budget);
        return toStatus(user, budget, DateUtil.range(req.month()));
    }

    @Transactional
    public void delete(User user, Long id) {
        Budget b = budgetRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Budget not found"));
        budgetRepo.delete(b);
    }

    BudgetStatusDto toStatus(User user, Budget b, LocalDate[] range) {
        BigDecimal spent = txRepo.sumByCategory(user, b.getCategory(), range[0], range[1]);
        BigDecimal limit = b.getLimitAmount();
        int percent = limit.signum() > 0
                ? spent.multiply(BigDecimal.valueOf(100)).divide(limit, 0, RoundingMode.HALF_UP).intValue()
                : 0;
        boolean over = spent.compareTo(limit) > 0;
        boolean warning = !over && percent >= 80;
        return new BudgetStatusDto(b.getId(), b.getCategory().getId(), b.getCategory().getName(), b.getMonth(),
                limit, spent, limit.subtract(spent), percent, over, warning);
    }
}
