package org.jumpmind.pos.service.strategy;

import org.jumpmind.pos.service.EndpointInvocationContext;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.util.clientcontext.ClientContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.jumpmind.pos.service.Endpoint.IMPLEMENTATION_TRAINING;
import static org.jumpmind.pos.service.util.EndpointUtils.getPathToEndpoint;

import static java.util.Optional.ofNullable;

@Component(LocalOnlyStrategy.LOCAL_ONLY_STRATEGY)
public class LocalOnlyStrategy implements IInvocationStrategy {
    static final String LOCAL_ONLY_STRATEGY = "LOCAL_ONLY";

    @Autowired
    protected ClientContext clientContext;

    private final Map<Method, Method> defaultMethodMap = new HashMap<>();
    private final Map<Method, Method> trainingMethodMap = new HashMap<>();

    public String getStrategyName() {
        return LOCAL_ONLY_STRATEGY;
    }

    @Override
    public Object invoke(EndpointInvocationContext endpointInvocationContext) throws Throwable {
        final Object endpointObj = getEndpointObject(endpointInvocationContext);
        final Method method = endpointInvocationContext.getMethod();
        final Map<Method, Method> methodMap = getMethodMapForDeviceMode();

        Method targetMethod = methodMap.get(method);
        if (targetMethod == null) {
            targetMethod = endpointObj.getClass().getMethod(method.getName(), method.getParameterTypes());
            methodMap.put(method, targetMethod);
        }

        try {
            return targetMethod.invoke(endpointObj, endpointInvocationContext.getArguments());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private String getDeviceMode() {
        String deviceMode = clientContext.get("deviceMode");
        return (deviceMode == null ? "default" : deviceMode);
    }

    private Object getEndpointObject(EndpointInvocationContext endpointInvocationContext) {
        return ofNullable(endpointInvocationContext.getEndpoint()).orElseThrow(() -> {
            final String path = getPathToEndpoint(endpointInvocationContext.getProxy(), endpointInvocationContext.getMethod());

            return new PosServerException(
                    String.format("No endpoint found for path '%s' Please define a Spring-discoverable @Component class, "
                            + "with a method annotated like  @Endpoint(path=\"%s\")", path, path));
        });
    }

    private Map<Method, Method> getMethodMapForDeviceMode() {
        return (IMPLEMENTATION_TRAINING.equalsIgnoreCase(getDeviceMode())) ? trainingMethodMap : defaultMethodMap;
    }
}
