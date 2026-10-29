package com.example.e2e.driver;

import com.example.e2e.config.Config;
import com.example.e2e.config.ConfigKeys;
import org.openqa.selenium.Dimension;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The browser-independent settings a {@link BrowserSetup} needs. Built from {@link Config} by
 * {@link WebDriverFactory}, so browser setups don't depend on the configuration mechanism.
 *
 * @param headless run without a visible window
 * @param windowSize window size in pixels
 * @param extraArgs additional browser command-line arguments
 */
public record BrowserSettings(boolean headless, Dimension windowSize, List<String> extraArgs) {

    /**
     * Reads the settings from configuration.
     *
     * @param config source of {@code headless}, {@code window.size} and {@code browser.args}
     * @return the settings
     * @throws NumberFormatException if {@code window.size} is not {@code <width>x<height>}
     */
    public static BrowserSettings from(Config config) {
        String[] size = config.get(ConfigKeys.WINDOW_SIZE).toLowerCase(Locale.ROOT).split("x");
        List<String> args = config.find(ConfigKeys.BROWSER_ARGS)
                .map(v -> Arrays.stream(v.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList())
                .orElse(List.of());
        return new BrowserSettings(config.headless(),
                new Dimension(Integer.parseInt(size[0].trim()), Integer.parseInt(size[1].trim())), args);
    }
}
