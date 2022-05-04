package org.jumpmind.pos.core.flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.util.event.AppEvent;
import org.jumpmind.pos.util.event.EventSource;
import org.jumpmind.pos.util.event.OnEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventBroadcasterTest {
    final IStateManager stateManager = mock(IStateManager.class);
    EventBroadcaster eventBroadcaster;

    @Before
    public void setup() {
        when(stateManager.getDevice()).thenReturn(new Device("pos", "001"));
        eventBroadcaster = new EventBroadcaster(stateManager);
    }

    @Test
    public void eventRaisesFromSelf() {
        final Target target = new Target();
        final AppEvent event = new TestEvent("001", "pos");
        boolean handled = eventBroadcaster.postEventToObject(Target.class, target, event);

        assertTrue(handled);

        TargetValues.builder()
                .onSelfImplicitEvent(event)
                .onSelfExplicitEvent(event)
                .withNoArgs(true)
                .build()
                .assertMatch(target.result);
    }

    @Test
    public void eventRaisesFromParent() {
        // Device 005
        //   |- Device 001
        when(stateManager.getParentDevice()).thenReturn(new Device("pos", "005"));

        final Target target = new Target();
        final AppEvent event = new TestEvent("005", "pos");
        boolean handled = eventBroadcaster.postEventToObject(Target.class, target, event);

        assertTrue(handled);

        TargetValues.builder()
                .onParentEvent(event)
                .build()
                .assertMatch(target.result);
    }

    @Test
    public void eventRaisesFromChild() {
        // Device 001
        //   |- Device 006
        when(stateManager.getChildDevices()).thenReturn(new ArrayList<Device>() {{
            add(new Device("customerdisplay", "006"));
        }});

        final Target target = new Target();
        final AppEvent event = new TestEvent("006", "customerdisplay");
        boolean handled = eventBroadcaster.postEventToObject(Target.class, target, event);

        assertTrue(handled);

        TargetValues.builder()
                .onChildEvent(event)
                .build()
                .assertMatch(target.result);
    }

    @Test
    public void eventsThatHandleMultipleSourcesShouldOnlyInvokeOnce() {
        // Device 002
        //   |- Device 001
        //      |- Device 003

        when(stateManager.getParentDevice()).thenReturn(new Device("pos", "002"));
        when(stateManager.getChildDevices()).thenReturn(new ArrayList<Device>() {{
            add(new Device("customerdisplay", "003"));
        }});

        final TargetMultipleSources target = new TargetMultipleSources();
        final AppEvent event = new TestEvent("001", "pos");
        boolean handled = eventBroadcaster.postEventToObject(TargetMultipleSources.class, target, event);

        assertTrue(handled);
        assertEquals(1, target.argInvocationCounter);
        assertEquals(1, target.noArgsInvocationCounter);
    }

    @Test
    public void subClassFilteringTest() {
        final SubTypeTargets targets = new SubTypeTargets();

        boolean handled = eventBroadcaster.postEventToObject(SubTypeTargets.class, targets, new TestSubEventTypeA("001", "pos"));

        assertTrue(handled);
        assertEquals(1, targets.straitBaseClassInvocations);
        assertEquals(1, targets.baseClassArgOfTypeFiltering);
        assertEquals(0, targets.subEventBArg);

        handled = eventBroadcaster.postEventToObject(SubTypeTargets.class, targets, new TestSubEventTypeB("001", "pos"));

        assertTrue(handled);
        assertEquals(2, targets.straitBaseClassInvocations);
        assertEquals(1, targets.baseClassArgOfTypeFiltering);
        assertEquals(1, targets.subEventBArg);
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TargetValues {
        AppEvent onSelfImplicitEvent;
        AppEvent onSelfExplicitEvent;
        AppEvent onParentEvent;
        AppEvent onChildEvent;
        boolean withNoArgs;

        public void assertMatch(TargetValues value) {
            assertNullOrEqual(onSelfImplicitEvent, value.onSelfImplicitEvent);
            assertNullOrEqual(onSelfExplicitEvent, value.onSelfExplicitEvent);
            assertNullOrEqual(onParentEvent, value.onParentEvent);
            assertNullOrEqual(onChildEvent, value.onChildEvent);
            assertBool(withNoArgs, value.withNoArgs);
        }

        private void assertBool(boolean expected, boolean value) {
            if (expected) {
                assertTrue(value);
            } else {
                assertFalse(value);
            }
        }

        private void assertNullOrEqual(Object expected, Object value) {
            if (expected == null) {
                assertNull(value);
            } else {
                assertEquals(expected, value);
            }
        }
    }

    static class Target {
        TargetValues result = new TargetValues();

        @OnEvent
        public void onImplicitSelfEvent(TestEvent event) {
            result.onSelfImplicitEvent = event;
        }

        @OnEvent(sources = { EventSource.SELF })
        public void onExplicitSelfEvent(TestEvent event) {
            result.onSelfExplicitEvent = event;
        }

        @OnEvent(sources = {
                EventSource.PARENT
        })
        public void onEventFromParentEvent(TestEvent event) {
            result.onParentEvent = event;
        }

        @OnEvent(sources = {
                EventSource.PAIRED
        })
        public void onEventFromChild(TestEvent event) {
            result.onChildEvent = event;
        }

        @OnEvent
        public void withNoArgs() {
            result.withNoArgs = true;
        }
    }

    static class TargetMultipleSources {
        int argInvocationCounter = 0;
        int noArgsInvocationCounter = 0;

        @OnEvent(
                sources = {
                        EventSource.SELF,
                        EventSource.PARENT,
                        EventSource.PAIRED,

                        // Repeat on purpose
                        EventSource.SELF,
                        EventSource.PARENT,
                        EventSource.PAIRED
                }
        )
        public void onArgEvent(AppEvent event) {
            argInvocationCounter++;
        }

        @OnEvent(
                sources = {
                        EventSource.SELF,
                        EventSource.PARENT,
                        EventSource.PAIRED,

                        // Repeat on purpose
                        EventSource.SELF,
                        EventSource.PARENT,
                        EventSource.PAIRED
                }
        )
        public void onNoArgEvent() {
            noArgsInvocationCounter++;
        }
    }

    static class TestEvent extends AppEvent {
        public TestEvent(String deviceId, String appId) {
            super(deviceId, appId);
        }
    }

    static abstract class TestBaseEventType extends AppEvent {
        public TestBaseEventType(String deviceId, String appId) {
            super(deviceId, appId);
        }
    }
    static class TestSubEventTypeA extends TestBaseEventType {
        public TestSubEventTypeA(String deviceId, String appId) {
            super(deviceId, appId);
        }
    }
    static class TestSubEventTypeB extends TestBaseEventType {
        public TestSubEventTypeB(String deviceId, String appId) {
            super(deviceId, appId);
        }
    }

    static class SubTypeTargets {
        int straitBaseClassInvocations = 0;
        int baseClassArgOfTypeFiltering = 0;
        int subEventBArg = 0;

        @OnEvent
        public void onStraitBaseClass(TestBaseEventType baseClass) {
            straitBaseClassInvocations += 1;
        }

        @OnEvent(ofTypes = TestSubEventTypeA.class)
        public void onBaseClassArgOfTypeFiltering(TestBaseEventType baseClass) {
            baseClassArgOfTypeFiltering += 1;
        }

        @OnEvent
        public void onTestSubEventBArg(TestSubEventTypeB baseClass) {
            subEventBArg += 1;
        }
    }
}
