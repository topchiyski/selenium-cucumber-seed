package com.example.e2e.driver;

import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.AbstractDriverOptions;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shared setup for Chromium-based browsers (Chrome, Edge).
 *
 * @param <O> the browser's options type
 */
public abstract class ChromiumSetup<O extends ChromiumOptions<O>> implements BrowserSetup {

    /** Constructor for subclasses. */
    protected ChromiumSetup() {
    }

    /**
     * Creates empty options of the concrete browser type.
     *
     * @return new browser options
     */
    protected abstract O newOptions();

    /**
     * Vendor-prefixed capability for console logging, e.g. {@code goog:loggingPrefs}.
     *
     * @return the capability name
     */
    protected abstract String loggingPrefsCapability();

    /**
     * Arguments only this browser needs.
     *
     * @return additional arguments; none by default
     */
    protected List<String> browserArgs() {
        return List.of();
    }

    /**
     * Builds options shared by all Chromium browsers: headless mode and window size, extra arguments,
     * no password-manager pop-ups, and console logging enabled.
     *
     * @param settings the browser settings
     * @return the browser options
     */
    @Override
    public AbstractDriverOptions<?> options(BrowserSettings settings) {
        O options = newOptions();
        if (settings.headless()) {
            options.addArguments("--headless=new",
                    "--window-size=" + settings.windowSize().getWidth() + "," + settings.windowSize().getHeight());
        }
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments(browserArgs());
        options.addArguments(settings.extraArgs());
        options.setExperimentalOption("prefs", noPasswordManagerPrefs());
        LoggingPreferences logging = new LoggingPreferences();
        logging.enable(LogType.BROWSER, Level.ALL);
        options.setCapability(loggingPrefsCapability(), logging);
        return options;
    }

    /**
     * Chromium browsers expose console logs through WebDriver's logging API.
     *
     * @return {@code true}
     */
    @Override
    public boolean supportsConsoleLogs() {
        return true;
    }

    /** Stops the "save/change password" bubbles from covering the page after logins. */
    private static Map<String, Object> noPasswordManagerPrefs() {
        return Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false);
    }
}
