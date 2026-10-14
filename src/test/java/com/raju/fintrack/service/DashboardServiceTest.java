package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.MonthlyDto;
import com.raju.fintrack.dto.Dtos.SummaryDto;
import com.raju.fintrack.entity.TransactionType;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.repository.AccountRepository;
import com.raju.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock TransactionRepository txRepo;
    @Mock AccountRepository accountRepo;
    @InjectMocks DashboardService service;

    @Test
    void summaryCalculatesNetAndBalance() {
        User user = new User();
        when(txRepo.sumByType(any(User.class), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000"));
        when(txRepo.sumByType(any(User.class), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("400"));
        when(accountRepo.totalBalance(user)).thenReturn(new BigDecimal("5000"));

        SummaryDto s = service.summary(user, "2026-10");

        assertEquals("2026-10", s.month());
        assertEquals(new BigDecimal("600"), s.net());
        assertEquals(new BigDecimal("5000"), s.totalBalance());
    }

    @Test
    void monthlyReturnsTwelveMonthsAndFillsGapsWithZero() {
        User user = new User();
        YearMonth now = YearMonth.now();
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{now.getYear(), now.getMonthValue(), TransactionType.EXPENSE, new BigDecimal("100")});
        when(txRepo.monthlyTotals(any(User.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(rows);

        List<MonthlyDto> result = service.monthly(user);

        assertEquals(12, result.size());
        MonthlyDto last = result.get(11);
        assertEquals(now.toString(), last.month());
        assertEquals(0, new BigDecimal("100").compareTo(last.expense()));
        assertEquals(0, BigDecimal.ZERO.compareTo(last.income()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(0).expense()));
    }
}
