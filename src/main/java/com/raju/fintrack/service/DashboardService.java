package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.CategoryTotal;
import com.raju.fintrack.dto.Dtos.MonthlyDto;
import com.raju.fintrack.dto.Dtos.SummaryDto;
import com.raju.fintrack.entity.TransactionType;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.repository.AccountRepository;
import com.raju.fintrack.repository.TransactionRepository;
import com.raju.fintrack.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;

    @Transactional(readOnly = true)
    public SummaryDto summary(User user, String month) {
        YearMonth ym = DateUtil.parse(month);
        LocalDate[] r = DateUtil.range(month);

        BigDecimal income = txRepo.sumByType(user, TransactionType.INCOME, r[0], r[1]);
        BigDecimal expense = txRepo.sumByType(user, TransactionType.EXPENSE, r[0], r[1]);
        BigDecimal totalBalance = accountRepo.totalBalance(user);

        // average monthly spending over the 12 months ending at the selected month
        BigDecimal last12 = txRepo.sumByType(user, TransactionType.EXPENSE, ym.minusMonths(11).atDay(1), r[1]);
        BigDecimal avg = last12.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        return new SummaryDto(ym.toString(), income, expense, income.subtract(expense), totalBalance, avg);
    }

    /** Income vs expense for the last 12 months (months with no data are zero-filled). */
    @Transactional(readOnly = true)
    public List<MonthlyDto> monthly(User user) {
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(11);
        List<Object[]> rows = txRepo.monthlyTotals(user, start.atDay(1), end.atEndOfMonth());

        Map<String, BigDecimal[]> byMonth = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            byMonth.put(start.plusMonths(i).toString(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (Object[] row : rows) {
            String key = String.format("%04d-%02d", ((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            BigDecimal[] slot = byMonth.get(key);
            if (slot == null) continue;
            BigDecimal amount = new BigDecimal(row[3].toString());
            if (row[2] == TransactionType.INCOME) slot[0] = amount; else slot[1] = amount;
        }
        List<MonthlyDto> result = new ArrayList<>();
        byMonth.forEach((m, v) -> result.add(new MonthlyDto(m, v[0], v[1])));
        return result;
    }

    @Transactional(readOnly = true)
    public List<CategoryTotal> byCategory(User user, String month, TransactionType type) {
        LocalDate[] r = DateUtil.range(month);
        return txRepo.totalsByCategory(user, type, r[0], r[1]).stream()
                .map(row -> new CategoryTotal((String) row[0], new BigDecimal(row[1].toString())))
                .toList();
    }
}
