package org.jumpmind.pos.core.service;

import io.prometheus.client.Gauge;
import org.apache.commons.lang3.StringUtils;

// Since the session metrics is processed between two different classes, uses this has a helper in-between the two
// to hopefully prevent some errors in recording.
class SubscribedSessionMetric {
    static final Gauge SUBSCRIBED_SESSIONS = Gauge.build()
            .name("session_subscribed")
            .help("the number of open connections to the server")
            .labelNames("device_id")
            .register();

    private SubscribedSessionMetric() {}

    public static void inc(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return;
        }

        SUBSCRIBED_SESSIONS
                .labels(deviceId)
                .inc();
    }

    public static void dec(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return;
        }

        SUBSCRIBED_SESSIONS
                .labels(deviceId)
                .dec();
    }
}
