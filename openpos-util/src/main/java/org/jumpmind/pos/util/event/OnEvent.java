/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * http://www.gnu.org/licenses.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.util.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate {@link AppEvent} method handlers.
 *
 * <p>
 * Only handler methods with 0 or 1 argument are supported. If the handler has 1 argument, then the argument's type must
 * be {@link AppEvent} or a derivation of it. Choosing to use a derivation of {@link AppEvent} as an argument will
 * implicitly filter events to any type that the event instance may be assigned to. You may also use {@link OnEvent#ofTypes()}
 * to filter accepted types further. Zero argument handlers are invoked when any event raised unless filtered using
 * {@link OnEvent#ofTypes()}.
 *
 * <pre>
 * {@code
 * @OnEvent
 * public void onAnyEvent(AppEvent event) {
 *     // Any event from oneself
 * }
 * }
 * </pre>
 *
 * <pre>
 * {@code
 * @OnEvent(sources = { EventSource.PAIRED })
 * public void onChildConnected(DeviceConnectedEvent event) {
 *     // Only the `DeviceConnectedEvent` raised from a child device
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEvent {
    /**
     * A list of source types that should be handled.
     *
     * <p>
     * By default, this is events are only received from one's own scope ({@link EventSource#SELF}), however you might
     * want to listen for events from attached devices ({@link EventSource#PAIRED}) or the device that your device is
     * attached to ({@link EventSource#PARENT}). This is useful in application scenarios like having `pos` with a child
     * `customerdisplay` app.
     *
     * @return An array of all event sources that are accepted by the handler.
     */
    EventSource[] sources() default { EventSource.SELF };

    /**
     * A list of all types accepted by the event handler.
     *
     * <p>
     * Filtering is implicitly done by the handler's argument type. In some scenarios, like deciding to use a base event
     * to catch multiple similar events, you may want to filter only certain events that implement the base type. If
     * left unspecified no filtering is applied.
     *
     * @return A list of all types to filter on.
     */
    Class<? extends AppEvent>[] ofTypes() default {};
}
