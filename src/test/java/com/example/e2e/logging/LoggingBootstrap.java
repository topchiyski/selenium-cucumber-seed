package com.example.e2e.logging;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Routes java.util.logging (used by Selenium) into SLF4J. Called from the earliest entry points
 * (TestNG listener, Cucumber plugin) so it's active whether tests start from Gradle or an IDE.
 * Levels are kept in sync with Logback by the {@code LevelChangePropagator} in logback-test.xml.
 */
public final class LoggingBootstrap {

    private static final AtomicBoolean INSTALLED = new AtomicBoolean();

    private LoggingBootstrap() {
    }

    /** Installs the java.util.logging bridge; safe to call more than once. */
    public static void install() {
        if (INSTALLED.compareAndSet(false, true)) {
            SLF4JBridgeHandler.removeHandlersForRootLogger(); // otherwise JUL also prints to stderr
            SLF4JBridgeHandler.install();
        }
    }
}
