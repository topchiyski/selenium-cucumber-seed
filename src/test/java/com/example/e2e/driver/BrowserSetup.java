package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;

/**
 * Everything that differs between browsers. To support a new browser, implement this interface and add a
 * constant to {@link BrowserType}; {@link WebDriverFactory} handles the rest (remote/local, timeouts, logging).
 */
public interface BrowserSetup {

    /**
     * Browser options/capabilities for the given settings; used for both local and remote sessions.
     *
     * @param settings the browser-independent settings
     * @return options for this browser
     */
    AbstractDriverOptions<?> options(BrowserSettings settings);

    /**
     * Starts a local browser with options previously created by {@link #options}.
     *
     * @param options options from {@link #options(BrowserSettings)}
     * @return the started session
     */
    WebDriver startLocal(AbstractDriverOptions<?> options);

    /**
     * Whether the browser exposes its console through WebDriver's logging API.
     *
     * @return {@code true} if console logs can be read; {@code false} by default
     */
    default boolean supportsConsoleLogs() {
        return false;
    }
}
