package com.portfoliomanager.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * Adapter between java.time (LocalDate/Instant) and java.util.Calendar.
 * This is the ONLY place Calendar is allowed to appear, keeping legacy date formats
 * out of the rest of the application codebase (specifically for yahoofinance-api).
 */
public final class CalendarConverter {

    private CalendarConverter() {}

    public static Calendar fromLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return calendar;
    }

    public static LocalDate toLocalDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return Instant.ofEpochMilli(calendar.getTimeInMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Calendar fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(instant.toEpochMilli());
        return calendar;
    }

    public static Instant toInstant(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return Instant.ofEpochMilli(calendar.getTimeInMillis());
    }
}
