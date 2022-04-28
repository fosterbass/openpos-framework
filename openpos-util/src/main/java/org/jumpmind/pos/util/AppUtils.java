package org.jumpmind.pos.util;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.MDC;
import static org.apache.commons.lang3.StringUtils.*;

@Slf4j
public final class AppUtils {

    static AtomicReference<String> HOST_NAME = new AtomicReference<String>(null);

    private static FastDateFormat timezoneFormatter = FastDateFormat.getInstance("Z");

    private AppUtils() {
    }

    public static void setupLogging(String deviceId) {
        if (isNotBlank(deviceId)) {
            MDC.put("stateManager", String.format("%s", deviceId));
            MDC.put("deviceId", deviceId);
        }
    }
    
    public static boolean isDevMode() {
        String value = System.getProperty("profile");
        if (!StringUtils.isEmpty(value)) {
            return value.equalsIgnoreCase("dev");
        } else {
            return false;
        }
    }

    static Date getLocalDateForOffset(long epochMillis, String timezoneOffset) {
        if (StringUtils.isNotBlank(timezoneOffset)) {
            int myOffset = TimeZone.getDefault().getOffset(epochMillis);
            int theirOffset = TimeZone.getTimeZone("GMT" + timezoneOffset).getOffset(epochMillis);
            return new Date(epochMillis - myOffset + theirOffset);
        } else {
            return new Date(epochMillis);
        }
    }

    public static Date getLocalDateForOffset(String timezoneOffset) {
        return getLocalDateForOffset(System.currentTimeMillis(), timezoneOffset);
    }

    public static String getTimezoneOffset() {
        String tz = timezoneFormatter.format(new Date());
        if (tz != null && tz.length() == 5) {
            return tz.substring(0, 3) + ":" + tz.substring(3, 5);
        }
        return null;
    }

    public static int getAvailableProcessors() {
        int numberOfCores = -1;
        try {
            numberOfCores = Runtime.getRuntime().availableProcessors();
            if (numberOfCores > 0) {
                return numberOfCores;
            } else {
                log.warn("Number of cores reported: " + numberOfCores + " - defaulting to 1");
                return 1;
            }
        } catch (Exception ex) {
            log.warn("Failed to determine number of cores on this system.", ex);
            return 1;
        }
    }

    public static String getHostName() {
        if (HOST_NAME.get() == null) {
            try {
                String hostName = System.getenv("HOSTNAME");
                
                if (StringUtils.isBlank(hostName)) {
                    hostName = System.getenv("COMPUTERNAME");
                }

                if (StringUtils.isBlank(hostName)) {
                    try {
                        hostName = IOUtils.toString(Runtime.getRuntime().exec("hostname").getInputStream(), Charset.defaultCharset());
                    } catch (Exception ex) {}
                }
                
                if (StringUtils.isBlank(hostName)) {
                    hostName = InetAddress.getByName(
                            InetAddress.getLocalHost().getHostAddress()).getHostName();
                }
                
                if (StringUtils.isNotBlank(hostName)) {
                    hostName = hostName.trim();
                }
                HOST_NAME.compareAndSet(null, hostName);
            } catch (Exception ex) {
                log.info("Unable to lookup hostname: " + ex);
            }
        }
        return HOST_NAME.get();
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            log.debug("Thread sleep interrupted.", ex);
        }
    }
    public static Date getTimezoneOffsetCorrectedDate(Date serverDate, int serverOffsetMillis, String clientOffsetString) {
        String strServerOffset = getTimezone(serverOffsetMillis);
        return getTimezoneOffsetCorrectedDate(serverDate, strServerOffset, clientOffsetString);
    }

    // TODO: Should be either dealing with UTC or client timezone, not trying to convert from unknown server TZ to client tz
    public static Date getTimezoneOffsetCorrectedDate(Date serverDate, String serverOffsetString, String clientOffsetString) {
        if (StringUtils.equals(serverOffsetString, clientOffsetString)) {
            return serverDate;
        }
        if (serverOffsetString != null && clientOffsetString != null) {
            try {
                ZoneOffset serverOffset = ZoneOffset.of(serverOffsetString);
                ZoneOffset clientOffset = ZoneOffset.of(clientOffsetString);

                LocalDateTime serverLocalDateTime = serverDate.toInstant().atOffset(serverOffset).toLocalDateTime();
                LocalDateTime clientLocalDateTime = serverDate.toInstant().atOffset(clientOffset).toLocalDateTime();

                long minutesOffset = ChronoUnit.MINUTES.between(clientLocalDateTime, serverLocalDateTime);
                return Date.from(serverLocalDateTime.minusMinutes(minutesOffset).toInstant(serverOffset));
            } catch (Exception e) {
                log.warn("Could not convert date using server and client offsets, returning original date", e);
                return serverDate;
            }
        }
        return serverDate;
    }

    /**
     * Converts offset to millis
     *
     */
    public static int convertOffsetToMillis(String offsetString) {
        ZoneOffset clientOffset = ZoneOffset.of(offsetString);
        return (int) (clientOffset.get(ChronoField.OFFSET_SECONDS) * 1000L);
    }

    /**
     * Converts offset in millis to timezone
     *
     * @param offsetMillis
     * @return
     */
    public static String getTimezone(int offsetMillis){
        int absOffset = Math.abs(offsetMillis);
        String strOffset = String.format("%s%02d:%02d", offsetMillis < 0 ? "-" : "+",
                TimeUnit.MILLISECONDS.toHours(absOffset),
                TimeUnit.MILLISECONDS.toMinutes(absOffset) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(absOffset))
        );
        return strOffset;
    }
}
