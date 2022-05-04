package org.jumpmind.pos.core.flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jumpmind.pos.util.event.AppEvent;
import org.jumpmind.pos.util.event.Event;
import org.jumpmind.pos.util.event.EventSource;
import org.jumpmind.pos.util.event.OnEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EventBroadcaster {

    IStateManager stateManager;

    public EventBroadcaster(IStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public <T> boolean postEventToObject(Class<T> clazz, Event event) {
        return postEventToObject(clazz, null, event);
    }

    public boolean postEventToObject(Object object, Event event) {
        if (object instanceof ScopeValue) {
            ScopeValue scopeValue = (ScopeValue)object;
            object = scopeValue.getValue();
        }
        if (object != null) {
            return postEventToObject(object.getClass(), object, event);
        } else {
            return false;
        }
    }

    @Builder
    @EqualsAndHashCode
    private static class EventKey {
        private Method handler;
        private EventSource acceptedSource;
    }

    private interface EventInvoker {
        void invoke(Object object, AppEvent arg);
    }

    public <T> boolean postEventToObject(Class<T> clazz, Object object, Event event) {
        if (!(event instanceof AppEvent)) {
            return false;
        }

        final AppEvent appEvent = (AppEvent) event;
        final Map<EventSource, List<EventInvoker>> eventMapping = getSourceToInvokersForClass(clazz, appEvent.getClass());

        final String mySource = AppEvent.createSourceString(stateManager.getDevice().getAppId(), stateManager.getDevice().getDeviceId());
        final String parentSource;

        if (stateManager.getParentDevice() != null) {
            parentSource = AppEvent.createSourceString(
                    stateManager.getParentDevice().getAppId(),
                    stateManager.getParentDevice().getDeviceId()
            );
        } else {
            parentSource = null;
        }

        boolean isEventFromSelf = mySource.equals(appEvent.getSource());
        boolean isEventFromParent = parentSource != null && parentSource.equals(appEvent.getSource());
        boolean isEventFromChild = stateManager.getChildDevices()
                .stream()
                .map(d -> AppEvent.createSourceString(d.getAppId(), d.getDeviceId()))
                .anyMatch(s -> s.equals(appEvent.getSource()));

        boolean hasTarget = isEventFromSelf || isEventFromParent || isEventFromChild;

        if (hasTarget) {
            if (object == null) {
                try {
                    object = clazz.getDeclaredConstructor().newInstance();
                    stateManager.performInjections(object);
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new FlowException("Failed to create event handler of type " + clazz.getName(), ex);
                } catch (Exception ex) {
                    throw new FlowException("Failed to inject values on the event handler of type " + clazz.getName(), ex);
                }
            }

            final Object objectClosure = object;

            final List<EventInvoker> invokers = Stream.concat(
                    isEventFromSelf && eventMapping.containsKey(EventSource.SELF) ? eventMapping.get(EventSource.SELF).stream() : Stream.empty(),
                    Stream.concat(
                        isEventFromParent && eventMapping.containsKey(EventSource.PARENT) ? eventMapping.get(EventSource.PARENT).stream() : Stream.empty(),
                        isEventFromChild && eventMapping.containsKey(EventSource.PAIRED) ? eventMapping.get(EventSource.PAIRED) .stream(): Stream.empty()
                    )
            ).collect(Collectors.toList());

            invokers.forEach(invoker -> invoker.invoke(objectClosure, appEvent));

            return invokers.size() > 0;
        }

        return false;
    }

    private static final Map<ClassEventPair, Map<EventSource, List<EventInvoker>>> memoizedClassToEvent = new ConcurrentHashMap<>();

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class ClassEventPair {
        private Class<?> handlerClass;
        private Class<? extends AppEvent> eventClass;
    }

    private <T, U extends AppEvent> Map<EventSource, List<EventInvoker>> getSourceToInvokersForClass(Class<T> handlerClass, Class<U> eventClass) {
        return memoizedClassToEvent.computeIfAbsent(
                new ClassEventPair(handlerClass, eventClass),
                clazzKey -> MethodUtils.getMethodsListWithAnnotation(clazzKey.handlerClass, OnEvent.class, true, true)
                        .stream()
                        .filter(method -> {
                            final OnEvent annotation = method.getAnnotation(OnEvent.class);

                            final boolean filteredType =
                                    ArrayUtils.contains(annotation.ofTypes(), clazzKey.eventClass)
                                            || annotation.ofTypes() == null
                                            || annotation.ofTypes().length == 0;

                            return (filteredType && method.getParameters().length == 0)
                                    || (filteredType && method.getParameters().length == 1 && method.getParameterTypes()[0].isAssignableFrom(clazzKey.eventClass));
                        })
                        .flatMap(method -> {
                            final OnEvent annotation = method.getAnnotation(OnEvent.class);
                            final HashSet<EventSource> sources = new HashSet<>(Arrays.asList(annotation.sources()));

                            return sources.stream()
                                    .map(v ->
                                            EventKey.builder()
                                                    .handler(method)
                                                    .acceptedSource(v)
                                                    .build()
                                    );
                        })
                        .collect(
                                Collectors.toMap(
                                        key -> key.acceptedSource,
                                        key -> {
                                            final EventInvoker eventInvoker = (inst, arg) -> {
                                                boolean hasEventParam = key.handler.getParameters().length == 1;

                                                try {
                                                    key.handler.setAccessible(true);

                                                    log.debug("invoking @OnEvent handler '{}' for event type '{}'; event sourced from '{}'...", key.handler, arg.getClass(), arg.getSource());

                                                    if (hasEventParam) {
                                                        key.handler.invoke(inst, arg);
                                                    } else {
                                                        key.handler.invoke(inst);
                                                    }
                                                } catch (Exception ex) {
                                                    log.error("unknown error occurred while attempting to invoke the event handler '{}' for event type '{}'", key.handler, arg.getClass(), ex);
                                                }
                                            };

                                            return Stream.of(eventInvoker).collect(Collectors.toList());
                                        },

                                        (left, right) -> {
                                            left.addAll(right);
                                            return left;
                                        }
                                )
                        )
        );
    }
}
