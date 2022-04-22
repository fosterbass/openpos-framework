package org.jumpmind.pos.service;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
public abstract class AbstractServiceFactory {

    @Autowired
    private EndpointInvocationHandler dispatcher;

    @SuppressWarnings("unchecked")
    protected <T> T buildService(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { serviceInterface }, dispatcher);
    }
}
