package com.example.e2e.support;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Reads the browser console log, where the browser supports it ({@code BrowserSetup.supportsConsoleLogs()});
 * otherwise an explanatory message is returned instead.
 */
public class BrowserConsole {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserConsole.class);

    private final Browser browser;

    /**
     * Creates the reader.
     *
     * @param browser the scenario's browser
     */
    public BrowserConsole(Browser browser) {
        this.browser = browser;
    }

    /**
     * Reads the browser console. Console errors are also logged as a warning.
     *
     * @return one line per console entry, a note if the browser doesn't support this, or empty if no browser
     *         is running or the log can't be read
     */
    public Optional<String> collect() {
        if (!browser.isStarted()) {
            return Optional.empty();
        }
        if (!browser.type().setup().supportsConsoleLogs()) {
            return Optional.of("Browser console logs are not available for " + browser.type() + ".");
        }
        try {
            List<LogEntry> entries = browser.driver().manage().logs().get(LogType.BROWSER).getAll();
            long errors = entries.stream().filter(e -> e.getLevel().intValue() >= Level.SEVERE.intValue()).count();
            if (errors > 0) {
                LOG.warn("Browser console has {} error(s); see the 'Browser console log' attachment", errors);
            }
            LOG.debug("Collected {} browser console entries", entries.size());
            String logs = entries.stream().map(BrowserConsole::format).collect(Collectors.joining(System.lineSeparator()));
            return Optional.of(logs.isEmpty() ? "(browser console was empty)" : logs);
        } catch (RuntimeException e) {
            LOG.warn("Could not read browser logs: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static String format(LogEntry entry) {
        return Instant.ofEpochMilli(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage();
    }
}
