package org.jumpmind.pos.persist;

import lombok.Getter;

public class RowConsumerContext<T> {
    @Getter
    final private T row;

    public RowConsumerContext(T row) {
        this.row = row;
    }

    public void breakIteration() {
        // Note that there could be some stack unwinding perf hit but it is likely negligible. To truly make it feel
        // like a loop break this is the way.
        throw new BreakIterationException();
    }

    static class BreakIterationException extends RuntimeException { }
}
