package com.portfoliomanager.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class CalendarConverterTest {

    @Test
    void fromLocalDate_NullReturnsNull() {
        assertNull(CalendarConverter.fromLocalDate(null));
    }

    @Test
    void fromLocalDate_ValidDate() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        Calendar cal = CalendarConverter.fromLocalDate(date);

        assertNotNull(cal);
        LocalDate roundTripped = CalendarConverter.toLocalDate(cal);
        assertEquals(date, roundTripped);
    }

    @Test
    void toLocalDate_NullReturnsNull() {
        assertNull(CalendarConverter.toLocalDate(null));
    }

    @Test
    void toLocalDate_ValidCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.MARCH, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        LocalDate result = CalendarConverter.toLocalDate(cal);
        assertEquals(LocalDate.of(2025, 3, 10), result);
    }

    @Test
    void fromInstant_NullReturnsNull() {
        assertNull(CalendarConverter.fromInstant(null));
    }

    @Test
    void fromInstant_ValidInstant() {
        Instant instant = Instant.parse("2025-06-15T12:00:00Z");
        Calendar cal = CalendarConverter.fromInstant(instant);

        assertNotNull(cal);
        assertEquals(instant.toEpochMilli(), cal.getTimeInMillis());
    }

    @Test
    void toInstant_NullReturnsNull() {
        assertNull(CalendarConverter.toInstant(null));
    }

    @Test
    void toInstant_ValidCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(1718400000000L);

        Instant result = CalendarConverter.toInstant(cal);
        assertEquals(1718400000000L, result.toEpochMilli());
    }
}
