package com.example.e2e.driver;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Supported browsers, each with its {@link BrowserSetup}. Selected with the {@code browser} config key. */
public enum BrowserType {
    CHROME(new ChromeSetup()),
    FIREFOX(new FirefoxSetup()),
    EDGE(new EdgeSetup());

    private final BrowserSetup setup;

    BrowserType(BrowserSetup setup) {
        this.setup = setup;
    }

    public BrowserSetup setup() {
        return setup;
    }

    public static BrowserType from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String supported = Arrays.stream(values()).map(b -> b.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Unsupported browser '" + value + "'. Supported: " + supported, e);
        }
    }
}
