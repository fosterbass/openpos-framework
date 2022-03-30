package org.jumpmind.pos.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class AppUtilsTest {


    @Test
    public void getLocalDateForOffsetNegativeOffset() {
        checkLocalDateForOffset("-12:00");
        checkLocalDateForOffset("-11:00");
        checkLocalDateForOffset("-10:00");
        checkLocalDateForOffset("-09:30");
        checkLocalDateForOffset("-09:00");
        checkLocalDateForOffset("-08:00");
        checkLocalDateForOffset("-07:00");
        checkLocalDateForOffset("-06:00");
        checkLocalDateForOffset("-05:00");
        checkLocalDateForOffset("-04:00");
        checkLocalDateForOffset("-03:30");
        checkLocalDateForOffset("-03:30");
        checkLocalDateForOffset("-03:00");
        checkLocalDateForOffset("-02:00");
        checkLocalDateForOffset("-01:00");
    }

    @Test
    public void getLocalDateForOffsetPositiveOffset() {
        checkLocalDateForOffset("+01:00");
        checkLocalDateForOffset("+02:00");
        checkLocalDateForOffset("+03:00");
        checkLocalDateForOffset("+03:30");
        checkLocalDateForOffset("+04:00");
        checkLocalDateForOffset("+04:30");
        checkLocalDateForOffset("+05:00");
        checkLocalDateForOffset("+05:30");
        checkLocalDateForOffset("+05:45");
        checkLocalDateForOffset("+06:00");
        checkLocalDateForOffset("+06:30");
        checkLocalDateForOffset("+07:00");
        checkLocalDateForOffset("+08:00");
        checkLocalDateForOffset("+08:45");
        checkLocalDateForOffset("+09:00");
        checkLocalDateForOffset("+09:30");
        checkLocalDateForOffset("+10:00");
        checkLocalDateForOffset("+10:30");
        checkLocalDateForOffset("+11:00");
        checkLocalDateForOffset("+12:00");
        checkLocalDateForOffset("+12:45");
        checkLocalDateForOffset("+13:00");
        checkLocalDateForOffset("+14:00");
    }

    @Test
    public void getLocalDateForOffsetZeroOffset() {
        checkLocalDateForOffset("+00:00");
    }

    void checkLocalDateForOffset(String testOffsetStr) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.MARCH, 22, 6, 0, 0);
        Date date = calendar.getTime();
        Date actualDate = AppUtils.getLocalDateForOffset(date.getTime(), testOffsetStr);

        ZoneOffset serverOffset = ZoneOffset.of(AppUtils.getTimezoneOffset());
        ZoneOffset testOffset = ZoneOffset.of(testOffsetStr);

        Date someDate = new Date();
        LocalDateTime serverLocalDateTime = someDate.toInstant().atOffset(serverOffset).toLocalDateTime();
        LocalDateTime testLocalDateTime = someDate.toInstant().atOffset(testOffset).toLocalDateTime();

        long millisOffset = ChronoUnit.MILLIS.between(testLocalDateTime, serverLocalDateTime);
        Date expectedDate = new Date(date.getTime() - millisOffset);
        assertEquals(expectedDate, actualDate);
    }

}