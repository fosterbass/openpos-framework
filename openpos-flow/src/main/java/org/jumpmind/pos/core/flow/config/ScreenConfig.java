package org.jumpmind.pos.core.flow.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class ScreenConfig {

    protected String timeoutAction;
    protected Integer timeout;

    public ScreenConfig merge(ScreenConfig config) {
        if (StringUtils.isEmpty(timeoutAction)) {
            timeoutAction = config.getTimeoutAction();
        }
        if (timeout == null) {
            timeout = config.getTimeout();
        }
        return this;
    }
}
