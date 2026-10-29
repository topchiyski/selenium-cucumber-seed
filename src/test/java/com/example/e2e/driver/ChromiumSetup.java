package com.example.e2e.driver;

import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.AbstractDriverOptions;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** Shared setup for Chromium-based browsers (Chrome, Edge). */
public abstract class ChromiumSetup<O extends ChromiumOptions<O>> implements BrowserSetup {

    protected abstract O newOptions();

    /** Vendor-prefixed capability for console logging, e.g. {@code goog:loggingPrefs}. */
    protected abstract String loggingPrefsCapability();

    /** Arguments only this browser needs. */
    protected List<String> browserArgs() {
        return List.of();
    }

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
