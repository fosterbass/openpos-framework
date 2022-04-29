package org.jumpmind.pos.core.service;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Hidden
@Slf4j
public class ClientLogCollectorService {
    @Value("${openpos.clientLogCollector.timestampFormat:#{null}}")
    protected String timestampFormat;

    protected DateTimeFormatter timestampFormatter;

    @PostConstruct
    protected void init() {
        if (timestampFormat != null) {
            try {
                this.timestampFormatter = DateTimeFormatter.ofPattern(timestampFormat).withZone(ZoneId.systemDefault());
            } catch (Exception ex) {
                log.error("openpos.clientLogCollector.timestampFormat value of '{}' is not valid.", this.timestampFormat);
            }
        }
    }

    @PostMapping("api/appId/{appId}/deviceId/{deviceId}/clientlogs")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void clientLogs(
            @PathVariable String appId,
            @PathVariable String deviceId,
            @RequestBody List<ClientLogEntry> clientLogEntries) {

        MDC.put("appId", appId);
        MDC.put("deviceId", deviceId);

        for (ClientLogEntry clientLogEntry : clientLogEntries) {
            String message = clientLogEntry.getMessage();

            if (timestampFormatter != null) {
                MDC.put("timestamp", timestampFormatter.format(clientLogEntry.getTimestamp().toInstant()));
            } else {
                MDC.put("timestamp", clientLogEntry.getTimestamp().toString());
            }

            if (clientLogEntry.getType() != null) {
                switch (clientLogEntry.getType()) {
                    case info:
                    case log:
                        log.info(message);
                        break;
                    case warn:
                        log.warn(message);
                        break;
                    case error:
                        log.error(message);
                        break;
                    case debug:
                        log.debug(message);
                        break;
                }
            }

        }
    }
}
