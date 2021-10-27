package org.jumpmind.pos.service.util;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.PosServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public final class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    private static final String ISO_DATE_TIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String ISO_DATE_TIME_MILLIS_T = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String ISO_DATE_TIME_SECONDS = "yyyy-MM-dd HH:mm:ss";
    private static final String ISO_DATE_TIME_SECONDS_T = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_DATE = "yyyy-MM-dd";

    public static final String SAFE_NULL_STR_VALUE = "null";

    private DateUtils() {
    }

    private static final String[] FORMATS = new String[] {
            ISO_DATE_TIME_MILLIS,
            ISO_DATE_TIME_MILLIS_T,
            ISO_DATE_TIME_SECONDS,
            ISO_DATE_TIME_SECONDS_T,
            ISO_DATE
    };
    
    public static Date parseDateTimeISO(String date) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        Exception originalException = null;
        for (String format : FORMATS) {
            if (date.length() == format.length() || date.length() == format.length() - 2 || date.length() == format.length() + 4) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                try {
                    return dateFormat.parse(date);
                } catch (ParseException ex) {
                    originalException = ex;
                }
            }
        }
        if (originalException != null) {
            throw new PosServerException("Failed to parse date as ISO format: '" + date + "'", originalException);
        } else {
            throw new PosServerException("Failed to parse date as ISO format: '" + date + "'");
        }
    }

    public static String formatDateTimeISO(Date date) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_TIME_SECONDS);
            return dateFormat.format(date);
        }
        return SAFE_NULL_STR_VALUE;
    }

    public static long daysBetween(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (24 * 60 * 60 * 1000);
    }

    public static String changeFormat(String value, String existingFormat, String newFormat) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        try {
            Date date = new SimpleDateFormat(existingFormat).parse(value);
            return new SimpleDateFormat(newFormat).format(date);
        } catch (ParseException e) {
            logger.warn(String.format("Unable to change format of date '%s' from '%s' to '%s'", value, existingFormat, newFormat), e);
            return value;
        }
    }

    /**
     * Returns the date adjusted from the server timezone to the client timezone
     * @param date date to be converted
     * @param serverOffsetString string representation of server timezone offset
     * @param clientOffsetString string representation of client timezone offset
     * @return date adjusted from server timezone to client timezone
     */
    public static Date getTimezoneOffsetCorrectedDate(Date date, String serverOffsetString, String clientOffsetString) {
        if (StringUtils.equals(serverOffsetString, clientOffsetString)) {
            return date;
        }
        if (serverOffsetString != null && clientOffsetString != null) {
            try {
                ZoneOffset serverOffset = ZoneOffset.of(serverOffsetString);
                ZoneOffset clientOffset = ZoneOffset.of(clientOffsetString);

                LocalDateTime serverLocalDateTime = date.toInstant().atOffset(serverOffset).toLocalDateTime();
                LocalDateTime clientLocalDateTime = date.toInstant().atOffset(clientOffset).toLocalDateTime();

                long hoursOffset = ChronoUnit.HOURS.between(clientLocalDateTime, serverLocalDateTime);
                return Date.from(serverLocalDateTime.minusHours(hoursOffset).toInstant(serverOffset));
            } catch (Exception e) {
                logger.warn("Could not convert date using server and client offsets, returning original date");
                return date;
            }
        }
        return date;
    }
}
