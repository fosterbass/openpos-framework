package org.jumpmind.pos.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.filter.EndpointFilterManager;
import org.jumpmind.pos.service.strategy.IInvocationStrategy;
import org.springframework.data.util.Version;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Slf4j
public class EndpointInvocationContext {

    private IInvocationStrategy strategy;
    List<String> profileIds;
    ServiceSpecificConfig config;
    Object proxy;
    Method method;
    String endpointImplementation;
    Map<String, Object> endpointsByPathMap;
    private String clientVersionString;
    private Version clientVersion;
    private String endpointPath;
    private Object endpoint;
    private Object[] arguments;
    private Object result;

    public static class EndpointInvocationContextBuilder {
        public EndpointInvocationContextBuilder clientVersionString(String clientVersionString) {
            this.clientVersionString = clientVersionString;
            if (StringUtils.isNotEmpty(clientVersionString)) {
                clientVersion = Version.parse(clientVersionString);
                if (clientVersion.equals(EndpointFilterManager.VERSION_ZERO)) {
                    log.debug("Unparsable client version {}", clientVersionString);
                    clientVersion = null;
                }
            }
            return this;
        }
    }

}