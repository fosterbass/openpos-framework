package org.jumpmind.pos.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void returnClientCorrectedTimeAcrossDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "-04:00", "-07:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(25);
        assertThat(calendar.get(Calendar.HOUR)).isEqualTo(11);
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.PM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }

    @Test
    public void returnInputDateWhenOffsetsAreEqual() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "-04:00", "-04:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(26);
        assertThat(calendar.get(Calendar.HOUR)).isEqualTo(2);
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.AM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }

    @Test
    public void returnClientCorrectedTimeWhenOffsetIsPositive() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "-01:00", "+01:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(26);
        assertThat(calendar.get(Calendar.HOUR)).isEqualTo(4);
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.AM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }

    @Test
    public void returnClientCorrectedTimeWhenOffsetIsNegative() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "+01:00", "-01:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(26);
        assertThat(calendar.get(Calendar.HOUR)).isZero();
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.AM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }

    @Test
    public void returnClientCorrectedTimeWhenServerOffsetIsZero() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "+00:00", "-01:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(26);
        assertThat(calendar.get(Calendar.HOUR)).isEqualTo(1);
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.AM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }

    @Test
    public void returnInputTimeWithNullOffset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, null, "-01:00");
        assertThat(correctedDate).isEqualTo(date);
    }

    @Test
    public void returnInputTimeWithInvalidOffset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = AppUtils.getTimezoneOffsetCorrectedDate(date, "invalid offset", "-01:00");
        assertThat(correctedDate).isEqualTo(date);
    }
}