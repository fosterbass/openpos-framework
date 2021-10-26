package org.jumpmind.pos.service.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

    @Test
    public void returnClientCorrectedTimeAcrossDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.OCTOBER, 26, 2, 0);
        Date date = calendar.getTime();
        Date correctedDate = DateUtils.getTimezoneOffsetCorrectedDate(date, "-04:00", "-07:00");
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
        Date correctedDate = DateUtils.getTimezoneOffsetCorrectedDate(date, "-04:00", "-04:00");
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
        Date correctedDate = DateUtils.getTimezoneOffsetCorrectedDate(date, "-01:00", "+01:00");
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
        Date correctedDate = DateUtils.getTimezoneOffsetCorrectedDate(date, "+01:00", "-01:00");
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
        Date correctedDate = DateUtils.getTimezoneOffsetCorrectedDate(date, "+00:00", "-01:00");
        calendar.setTime(correctedDate);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2021);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.OCTOBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(26);
        assertThat(calendar.get(Calendar.HOUR)).isEqualTo(1);
        assertThat(calendar.get(Calendar.AM_PM)).isEqualTo(Calendar.AM);
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
    }
}