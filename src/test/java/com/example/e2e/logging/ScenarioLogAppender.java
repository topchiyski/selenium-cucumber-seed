package com.example.e2e.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.nio.charset.StandardCharsets;

/**
 * Logback appender that buffers the log lines of the scenario running on the current thread, so they
 * can be attached to the report. Each scenario runs on a single thread, which keeps parallel scenarios'
 * logs apart. Capturing is started/stopped by {@link ExecutionLogger}.
 */
public class ScenarioLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    /** Keeps a runaway log from exhausting memory; the attachment notes when it was cut. */
    private static final int MAX_CHARS = 2_000_000;
    private static final ThreadLocal<StringBuilder> BUFFER = new ThreadLocal<>();

    private Encoder<ILoggingEvent> encoder;

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("No encoder set for appender " + getName());
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        StringBuilder buffer = BUFFER.get();
        if (buffer == null || buffer.length() >= MAX_CHARS) {
            return;
        }
        buffer.append(new String(encoder.encode(event), StandardCharsets.UTF_8));
        if (buffer.length() >= MAX_CHARS) {
            buffer.append("... log truncated at ").append(MAX_CHARS).append(" characters").append(System.lineSeparator());
        }
    }

    public static void startCapture() {
        BUFFER.set(new StringBuilder());
    }

    /** The lines captured so far for the current thread's scenario (empty if not capturing). */
    public static String captured() {
        StringBuilder buffer = BUFFER.get();
        return buffer == null ? "" : buffer.toString();
    }

    public static void stopCapture() {
        BUFFER.remove();
    }
}
