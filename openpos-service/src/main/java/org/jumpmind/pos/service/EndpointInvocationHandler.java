package org.jumpmind.pos.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.strategy.IInvocationStrategy;
import org.jumpmind.pos.util.RestApiSupport;
import org.jumpmind.pos.util.clientcontext.ClientContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jumpmind.pos.service.Endpoint.IMPLEMENTATION_DEFAULT;
import static org.jumpmind.pos.service.Endpoint.IMPLEMENTATION_TRAINING;
import static org.jumpmind.pos.service.util.EndpointUtils.getPathToEndpoint;
import static org.jumpmind.pos.service.util.EndpointUtils.getRestController;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * A Java {@link Proxy} which intercepts all calls to JMC service methods and routes those calls to their appropriate endpoints by interrogating:
 * <ul>
 * <li>the runtime, profile-driven Spring context</li>
 * <li>any {@link RequestMapping} annotations on the service interface and/or service method</li>
 * <li>any {@link Endpoint} or {@link EndpointOverride} annotation on the endpoint class, with the latter taking precedence over the former</li>
 * </ul>
 */
@Slf4j
@Component
public class EndpointInvocationHandler implements InvocationHandler {
    @Autowired
    private EndpointInvoker endpointInvoker;

    @Autowired
    private EndpointManager endpointManager;

    @Autowired
    private EndpointInvocationHandleFactory endpointInvocationHandleFactory;

    @Autowired
    private Map<String, IInvocationStrategy> strategies;

    @Autowired
    private ServiceConfig serviceConfigs;

    @Autowired
    private ClientContext clientContext;

    @Override
    public Object invoke(Object service, Method serviceMethod, Object[] serviceMethodArgs) throws Throwable {
        if (serviceMethod.getName().equals("equals")) {
            return false;
        }

        final String path = getPathToEndpoint(service, serviceMethod);
        final String deviceMode = clientContext.get("deviceMode");
        final String implementation = (IMPLEMENTATION_TRAINING.equalsIgnoreCase(deviceMode)) ? IMPLEMENTATION_TRAINING : IMPLEMENTATION_DEFAULT;
        final ServiceSpecificConfig serviceConfig = getSpecificConfig(service, serviceMethod);

        Object endpointObj = endpointManager.getEndpointObject(implementation, path);

        EndpointSpecificConfig endpointConfig = null;
        String endpointImplementation = null;

        if (serviceConfig != null && StringUtils.isNotBlank(serviceConfig.getImplementation()) && service != null) {
            if (!serviceConfig.getImplementation().equalsIgnoreCase(endpointManager.getCurrentServiceImplementation(service))) {
                endpointManager.buildEndpointMappingsForService(service, serviceConfig.getImplementation());
                endpointObj = endpointManager.getEndpointObject(implementation, path);
            }
        }

        if (endpointObj != null) {
            EndpointOverride override = endpointObj.getClass().getAnnotation(EndpointOverride.class);

            if (override != null) {
                for (EndpointSpecificConfig endpointSpecificConfig : serviceConfig.getEndpoints()) {
                    if (override.path().equals(endpointSpecificConfig.getPath())) {
                        endpointImplementation = firstNonNull(endpointImplementation, override.implementation());
                        endpointConfig = endpointSpecificConfig;
                    }
                }
            } else {
                Endpoint endpoint = endpointObj.getClass().getAnnotation(Endpoint.class);

                if (endpoint != null) {
                    for (EndpointSpecificConfig endpointSpecificConfig : serviceConfig.getEndpoints()) {
                        String endpointPath = endpoint.path();
                        if (endpointPath.indexOf(RestApiSupport.REST_API_CONTEXT_PATH) >= 0) {
                            endpointPath = endpointPath.substring(RestApiSupport.REST_API_CONTEXT_PATH.length());
                        }
                        if (endpointPath.equals(endpointSpecificConfig.getPath())) {
                            endpointImplementation = firstNonNull(endpointImplementation, endpoint.implementation());
                            endpointConfig = endpointSpecificConfig;
                        }
                    }
                }
            }
        }

        IInvocationStrategy strategy;
        List<String> profileIds = new ArrayList<>();

        if ((endpointConfig != null) && (endpointConfig.getStrategy() != null)) {
            strategy = strategies.get(endpointConfig.getStrategy().name());

            if (isNotBlank(endpointConfig.getProfile())) {
                profileIds.add(endpointConfig.getProfile());
            }
        } else {
            strategy = strategies.get(serviceConfig.getStrategy().name());

            if (isNotEmpty(serviceConfig.getProfileIds())) {
                profileIds.addAll(serviceConfig.getProfileIds());
            }
        }

        final EndpointInvocationContext invocationContext = EndpointInvocationContext.builder()
                .profileIds(profileIds)
                .strategy(strategy)
                .config(serviceConfig)
                .proxy(service)
                .method(serviceMethod)
                .endpointImplementation(endpointImplementation)
                .clientVersionString(clientContext.get("version.nu-commerce"))
                .endpointPath(path)
                .endpoint(endpointObj)
                .arguments(serviceMethodArgs)
                .build();

        /*
         * If the service method has a return type of EndpointInvocationHandle, we're going to dispense a fluent API to the caller rather than
         * invoke the service method directly.  This approach affords the client to defer that invocation and override aspects of the execution
         * context which are pre-determined by its static directives and otherwise outside the calling client's control.
         *
         * Once the client calls execute() on the dispensed handle, the endpoint invoker will be called normally but will receive an
         * EndpointInvocationContext reflecting the client's preferences rather than the one we've derived to this point.
         */
        return (serviceMethod.getReturnType().equals(EndpointInvocationHandle.class))
                ? endpointInvocationHandleFactory.createServiceHandle(invocationContext)
                : endpointInvoker.invoke(invocationContext);
    }

    private ServiceSpecificConfig getSpecificConfig(Object service, Method method) {
        final String serviceName = getServiceName(service, method);
        final String deviceId = defaultString(clientContext.get("deviceId"), "no-device");

        return serviceConfigs.getServiceConfig(deviceId, serviceName);
    }

    private static String getServiceName(Object object, Method method) {
        RestController restController = getRestController(object, method);

        if ((restController == null) || isBlank(restController.value())) {
            throw new IllegalStateException(method.getDeclaringClass().getSimpleName() + " must declare @"
                    + RestController.class.getSimpleName() + " and it must have the value() attribute set");
        }
        return restController.value();
    }
}
