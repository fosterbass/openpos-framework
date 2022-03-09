package org.jumpmind.pos.util;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A wrapper around an SLF4J logger which exposes operations for producing "pretty" log output represented in rows and columns.
 *
 * @author Jason Weiss
 */
@Builder
public class PrettyTableLogger {
    /* TODO The interface here is kind of clumsy, especially when dealing with sub-rows.  It should be more fluent, and sub-rows should have access to
     * the object defining their parent row so we enforce the parent/child relationship (which currently is unenforced). */

    /* TODO I wanted to use an inner class to control the fluent order of execution (start, log..., end), but that screws up the output formatting
     * because the default appender pattern includes the invoking class.  In other words, the headers aren't lined up with the data columns because
     * the latter would belong to a longer-named class than the former.  Would be nice to fix this... */

    private static final String DEFAULT_FORMAT = "%s";
    private static final int DEFAULT_WIDTH = 120;
    private static final char BOUNDARY_CHAR = '+';
    private static final char BANNER_CHAR = '-';

    // the width of the table's fixed-width decorations; ignored when the table's contents exceed it
    @NonNull
    @Default
    private final Integer tableWidth = DEFAULT_WIDTH;

    // an optional banner to include at the top of the table
    private final String banner;

    // the printf-style format pattern to apply to the table's header
    @NonNull
    @Default
    private final String headerFormat = DEFAULT_FORMAT;

    // the printf-style format pattern to apply to the table's sub-header
    @NonNull
    @Default
    private final String subHeaderFormat = DEFAULT_FORMAT;

    // the printf-style format pattern to apply to the table's data columns
    @NonNull
    @Builder.Default
    private final String columnFormat = DEFAULT_FORMAT;

    // the printf-style format pattern to apply to the table's sub-level data columns
    @NonNull
    @Default
    private final String subColumnFormat = DEFAULT_FORMAT;

    // the content to include in the table's header
    @NonNull
    @Singular
    private final List<String> headers;

    // the content to include in the table's sub-header
    @Singular
    private final List<String> subHeaders;

    // the delegate logger (or other consumer) receiving this logger's line-by-line output
    @NonNull
    private final Consumer<String> writer;

    // @true if @writer will produce any output; @false if @writer is disabled and a minimum amount of work should be done by this logger
    @Getter
    @Default
    private final boolean writerEnabled = true;

    /**
     * Initializes a pretty table logger whose logging calls will be produced at the "debug" severity level.
     *
     * @param logger the SLF4J logger to wrap
     * @return a builder for a debug-level {@code PrettyTableLogger}
     */
    public static PrettyTableLoggerBuilder debug(Logger logger) {
        return builder().writer(logger::debug).writerEnabled(logger.isDebugEnabled());
    }

    /**
     * Initializes a pretty table logger whose logging calls will be produced at the "error" severity level.
     *
     * @param logger the SLF4J logger to wrap
     * @return a builder for an error-level {@code PrettyTableLogger}
     */
    public static PrettyTableLoggerBuilder error(Logger logger) {
        return builder().writer(logger::error).writerEnabled(logger.isErrorEnabled());
    }

    /**
     * Initializes a pretty table logger whose logging calls will be produced at the "info" severity level.
     *
     * @param logger the SLF4J logger to wrap
     * @return a builder for an info-level {@code PrettyTableLogger}
     */
    public static PrettyTableLoggerBuilder info(Logger logger) {
        return builder().writer(logger::info).writerEnabled(logger.isInfoEnabled());
    }

    /**
     * Initializes a pretty table logger whose logging calls will be produced at the "trace" severity level.
     *
     * @param logger the SLF4J logger to wrap
     * @return a builder for a trace-level {@code PrettyTableLogger}
     */
    public static PrettyTableLoggerBuilder trace(Logger logger) {
        return builder().writer(logger::trace).writerEnabled(logger.isTraceEnabled());
    }

    /**
     * Initializes a pretty table logger whose logging calls will be produced at the "warn" severity level.
     *
     * @param logger the SLF4J logger to wrap
     * @return a builder for a warn-level {@code PrettyTableLogger}
     */
    public static PrettyTableLoggerBuilder warn(Logger logger) {
        return builder().writer(logger::warn).writerEnabled(logger.isWarnEnabled());
    }

    /**
     * Indicates that all banners, headers, and rows have been logged.  A closing boundary line will be logged to indicate the "closing" of the table.
     */
    public void endLogging() {
        if (writerEnabled) {
            logDelimitedLine(BOUNDARY_CHAR);
        }
    }

    /**
     * Logs a single data row to the table.
     *
     * @param columns the columns defining the row
     * @return this logger
     */
    public PrettyTableLogger logRow(Object... columns) {
        return logRowImpl(columnFormat, columns);
    }

    /**
     * Logs a single supplied data row to the table.  Call this method instead of {@link #logRow(Object...)} when construction of the row is
     * non-trivial so that the effort won't be wasted if the underlying writer is disabled.
     *
     * @param columns the columns defining the row
     * @return this logger
     */
    public PrettyTableLogger logRowFrom(Supplier<Object[]> columns) {
        return (writerEnabled) ? logRow(columns.get()) : this;
    }

    /**
     * Logs multiple data rows to the table.
     *
     * @param rows the data rows to log
     * @return this logger
     */
    public PrettyTableLogger logRows(Stream<Object[]> rows) {
        return logRowsFrom(() -> rows);
    }

    /**
     * Logs multiple supplied data rows to the table.  Call this method instead of {@link #logRows(Stream)} when construction of the rows is
     * non-trivial so that the effort won't be wasted if the underlying writer is disabled.
     *
     * @param rows the data rows to log
     * @return this logger
     */
    public PrettyTableLogger logRowsFrom(Supplier<Stream<Object[]>> rows) {
        if (writerEnabled) {
            rows.get().forEach(this::logRow);
        }
        return this;
    }

    /**
     * Logs a single data sub-row to the table.
     *
     * @param columns the columns defining the sub-row
     * @return this logger
     */
    public PrettyTableLogger logSubRow(Object... columns) {
        return logRowImpl(subColumnFormat, columns);
    }

    /**
     * Logs a single supplied data sub-row to the table.  Call this method instead of {@link #logSubRow(Object...)} when construction of the rows is
     * non-trivial so that the effort won't be wasted if the underlying writer is disabled.
     *
     * @param columns the columns defining the sub-row
     * @return this logger
     */
    public PrettyTableLogger logSubRowFrom(Supplier<Object[]> columns) {
        return (writerEnabled) ? logSubRow(columns.get()) : this;
    }

    public PrettyTableLogger startLogging() {
        if (writerEnabled) {
            logBanner();
            logHeaders();
        }
        return this;
    }

    private void logBanner() {
        logDelimitedLine(BOUNDARY_CHAR);

        if (isNotBlank(banner)) {
            writer.accept(center(banner, tableWidth));
            logDelimitedLine(BANNER_CHAR);
        }
    }

    private void logDelimitedLine(char delimiter) {
        writer.accept(repeat(delimiter, tableWidth));
    }

    private void logHeaders() {
        writer.accept(String.format(headerFormat, headers.toArray()));

        if (isNotEmpty(subHeaders)) {
            writer.accept(String.format(subHeaderFormat, subHeaders.toArray()));
        }
        logDelimitedLine(BOUNDARY_CHAR);
    }

    private PrettyTableLogger logRowImpl(String format, Object... columns) {
        if (writerEnabled) {
            writer.accept(String.format(format, columns));
        }
        return this;
    }
}
