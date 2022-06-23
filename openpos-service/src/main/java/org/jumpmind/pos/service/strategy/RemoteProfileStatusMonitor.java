package org.jumpmind.pos.service.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.status.*;
import org.jumpmind.pos.util.web.ConfiguredRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RemoteProfileStatusMonitor extends AbstractStatusReporter {
    public static final String STATUS_NAME = "NETWORK.REMOTE";
    public static final String STATUS_ICON = "cloud";

    private Map<String, RemoteProfileStatusInfo> statuses = new ConcurrentHashMap<>();

    private RestTemplate template;

    @PostConstruct
    protected void init() {
        template = new ConfiguredRestTemplate(2);
        template.getInterceptors().removeIf(i -> "LoggingRequestInterceptor".equals(i.getClass().getSimpleName()));
    }

    @Override
    protected String getIdForLastStatus() {
        return clientContext != null ? clientContext.get(ClientContext.DEVICE_ID) : null;
    }

    @Override
    protected StatusReport getUnknownStatusReport() {
        return new StatusReport(STATUS_NAME, STATUS_ICON, Status.Unknown, "");
    }

    public boolean isOffline(String profileId) {
        return Status.Offline == this.statuses.getOrDefault(profileId, new RemoteProfileStatusInfo(profileId, Status.Online, "")).getStatus();
    }

    public Status getProfileStatus(String profileId) {
        return this.statuses.getOrDefault(profileId, new RemoteProfileStatusInfo(profileId, Status.Online, "")).getStatus();
    }

    public void setStatusUrl(String profileId, String baseUrl) {
        this.statuses.compute(profileId, (profId, status) -> {
            if (status == null) {
                return new RemoteProfileStatusInfo(profId, Status.Online, makePingUrl(baseUrl));
            } else {
                if (StringUtils.isBlank(status.getStatusUrl())) {
                    status.setStatusUrl(makePingUrl(baseUrl));
                }
                return status;
            }
        });
    }

    public void setStatus(String profileId, Status status) {
        this.setStatus(profileId, status, null);
    }

    public void setStatus(String profileId, Status status, String message) {
        if (StringUtils.isBlank(message)) {
            message = "";
        }

        this.statuses.compute(profileId, (profId, stat) -> {
            if (stat == null) {
                return new RemoteProfileStatusInfo(profId, Status.Online, "");
            } else {
                Status prevStatus = stat.getStatus();
                stat.setStatus(status);
                if (status != Status.Online && status != prevStatus) {
                    log.warn("Status of profile '{}' has now changed from {} to {}", profileId, prevStatus, status);
                }

                return stat;
            }
        });
        reportStatus(message);
    }

    protected String makePingUrl(String baseUrl) {
        return StringUtils.isNotBlank(baseUrl) ? String.format("%s%s", baseUrl, "/ping") : "";
    }

    private void reportStatus(String message) {

        Status lowestCommonDenominatorStatus = Status.Online;

        for (RemoteProfileStatusInfo rStatus : statuses.values()) {
            Status status = rStatus.getStatus();
            if (status == Status.Error || status == Status.Offline) {
                lowestCommonDenominatorStatus = status;
                break;
            }
        }

        this.recordAndPublishStatus(new StatusReport(STATUS_NAME, STATUS_ICON, lowestCommonDenominatorStatus, message));
    }

    @Scheduled(fixedDelayString = "${openpos.services.remoteServerOfflineCheckDelayMs:10000}")
    protected void updateProfileStatus() {
        this.statuses.entrySet().stream().filter(e -> Status.Offline == e.getValue().getStatus()).forEach( entry -> {
            ResponseEntity<Map<String,String>> response = null;
            try {
                log.debug("Pinging '{}' at {} to check if server is online...", entry.getValue().getProfileId(), entry.getValue().getStatusUrl());
                response = template.exchange(entry.getValue().getStatusUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, String>>(){});
            } catch (Exception ex) {
                log.debug("Status of profile '{}' is still {}. Ping error: {}", entry.getValue().getProfileId(), Status.Offline, ex.getMessage());
            }

            if (response != null && response.hasBody() && response.getBody().containsKey("pong")) {
                log.debug("Got response from {}: {}", entry.getValue().getStatusUrl(), response.getBody());
                this.setStatus(entry.getValue().getProfileId(), Status.Online);
                log.info("Status of profile '{}' has now changed from {} to {}", entry.getValue().getProfileId(), Status.Offline, Status.Online);
            }

        });
    }

    @Data
    @AllArgsConstructor
    public static class RemoteProfileStatusInfo {
        String profileId;
        Status status;
        String statusUrl;
    }
}
