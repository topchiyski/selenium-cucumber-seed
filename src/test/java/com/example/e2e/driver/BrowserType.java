package com.example.e2e.driver;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Supported browsers, each with its {@link BrowserSetup}. Selected with the {@code browser} config key. */
public enum BrowserType {
    /** Google Chrome. */
    CHROME(new ChromeSetup()),
    /** Mozilla Firefox. */
    FIREFOX(new FirefoxSetup()),
    /** Microsoft Edge. */
    EDGE(new EdgeSetup());

    private final BrowserSetup setup;

    BrowserType(BrowserSetup setup) {
        this.setup = setup;
    }

    /**
     * The browser-specific setup.
     *
     * @return the setup used to build options and start local sessions
     */
    public BrowserSetup setup() {
        return setup;
    }

    /**
     * Parses a {@code browser} config value.
     *
     * @param value browser name, case-insensitive, e.g. {@code chrome}
     * @return the browser type
     * @throws IllegalArgumentException if the name is not supported (the message lists supported names)
     */
    public static BrowserType from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String supported = Arrays.stream(values()).map(b -> b.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Unsupported browser '" + value + "'. Supported: " + supported, e);
        }
    }
}
