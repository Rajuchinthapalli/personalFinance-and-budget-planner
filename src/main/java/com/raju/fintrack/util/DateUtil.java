package com.raju.fintrack.util;

import java.time.LocalDate;
import java.time.YearMonth;

public final class DateUtil {
    private DateUtil() {}

    /** Parses yyyy-MM; null or blank means the current month. */
    public static YearMonth parse(String month) {
        return (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
    }

    /** Returns [firstDay, lastDay] of the month. */
    public static LocalDate[] range(String month) {
        YearMonth ym = parse(month);
        return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
    }
}
