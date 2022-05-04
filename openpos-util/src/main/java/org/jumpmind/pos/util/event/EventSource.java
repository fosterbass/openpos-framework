package org.jumpmind.pos.util.event;

/**
 * An enumeration of the different types of sources that are accepted by {@link OnEvent#sources()} filtering.
 */
public enum EventSource {

    /**
     * A {@link OnEvent#sources()} filter for {@link OnEvent} handler's that allows events published from one's own
     * device to be handled by the annotated method.
     */
    SELF,

    /**
     * A {@link OnEvent#sources()} filter for {@link OnEvent} handler's that allows events published from the parent of
     * {@link #SELF} to be handled by the annotated method.
     */
    PARENT,

    /**
     * A {@link OnEvent#sources()} filter for {@link OnEvent} handler's that allows events published from the paired
     * child of {@link #SELF} to be handled by the annotated method. A paired child is usually a device such as the
     * customer display.
     */
    PAIRED
}
