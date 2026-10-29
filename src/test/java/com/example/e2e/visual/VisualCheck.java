package com.example.e2e.visual;

import com.applitools.ICheckSettings;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.example.e2e.config.Config;
import com.example.e2e.config.ConfigKeys;
import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;
import org.picocontainer.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Applitools Eyes visual checks for one scenario. {@code VisualHooks} opens and closes the Eyes test around
 * {@code @visual} scenarios; steps add checkpoints with {@link #check(String, By...)}.
 * <p>
 * Each checkpoint uploads a full-page screenshot to Applitools, which compares it with the baseline for the
 * same app, test name, browser, OS and viewport. Without an API key ({@code APPLITOOLS_API_KEY}) or with
 * {@code applitools.enabled=false}, every method is a no-op and a warning is logged once per run.
 * <p>
 * PicoContainer creates one per scenario; {@link #dispose()} aborts an Eyes test that was never closed.
 */
public class VisualCheck implements Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(VisualCheck.class);
    private static final AtomicBoolean DISABLED_WARNING_LOGGED = new AtomicBoolean();

    private final Config config;
    private final Browser browser;
    private Eyes eyes;

    /**
     * Creates the visual checks for one scenario.
     *
     * @param config Applitools settings ({@code applitools.*})
     * @param browser the scenario's browser
     */
    public VisualCheck(Config config, Browser browser) {
        this.config = config;
        this.browser = browser;
    }

    /**
     * Whether checkpoints are sent to Applitools.
     *
     * @return {@code true} if an API key is configured and {@code applitools.enabled} isn't {@code false}
     */
    public boolean isEnabled() {
        return config.getBoolean(ConfigKeys.APPLITOOLS_ENABLED) && config.find(ConfigKeys.APPLITOOLS_API_KEY).isPresent();
    }

    /**
     * Starts the Eyes test for a scenario and sets the browser viewport to {@code applitools.viewport}.
     * Does nothing when visual checks are disabled.
     *
     * @param testName the baseline's test name, usually the scenario name
     * @throws IllegalStateException if no browser is running
     */
    public void open(String testName) {
        if (!isEnabled()) {
            if (DISABLED_WARNING_LOGGED.compareAndSet(false, true)) {
                LOG.warn("Applitools visual checks are skipped: set the APPLITOOLS_API_KEY environment variable to enable them");
            }
            return;
        }
        Eyes newEyes = new Eyes(VisualTestingRun.runner());
        newEyes.setLogHandler(new Slf4jEyesLogHandler());
        newEyes.setApiKey(config.get(ConfigKeys.APPLITOOLS_API_KEY));
        config.find(ConfigKeys.APPLITOOLS_SERVER_URL).ifPresent(newEyes::setServerUrl);
        newEyes.setBatch(VisualTestingRun.batch(config));
        newEyes.setMatchLevel(matchLevel(config.get(ConfigKeys.APPLITOOLS_MATCH_LEVEL)));
        newEyes.addProperty("Environment", config.env());

        String appName = config.get(ConfigKeys.APPLITOOLS_APP_NAME);
        newEyes.open(browser.driver(), appName, testName, viewport());
        eyes = newEyes;
        LOG.info("Eyes test opened: app '{}', test '{}', viewport {}, match level {}", appName, testName,
                config.get(ConfigKeys.APPLITOOLS_VIEWPORT), config.get(ConfigKeys.APPLITOOLS_MATCH_LEVEL));
    }

    /**
     * Adds a full-page checkpoint using the default match level.
     *
     * @param name checkpoint name shown in the dashboard, e.g. "Checkout overview"
     * @param ignoredRegions elements whose content changes between runs (dates, ads); they're not compared
     */
    public void check(String name, By... ignoredRegions) {
        check(name, null, ignoredRegions);
    }

    /**
     * Adds a full-page checkpoint with a specific match level.
     *
     * @param name checkpoint name shown in the dashboard
     * @param matchLevel match level for this checkpoint only, or {@code null} for {@code applitools.match-level}
     * @param ignoredRegions elements that are not compared
     */
    public void check(String name, MatchLevel matchLevel, By... ignoredRegions) {
        if (eyes == null) {
            LOG.debug("Visual checkpoint '{}' skipped (visual checks disabled)", name);
            return;
        }
        SeleniumCheckSettings target = Target.window().withName(name);
        if (matchLevel != null) {
            target = target.matchLevel(matchLevel);
        }
        if (ignoredRegions.length > 0) {
            target = target.ignore(ignoredRegions);
        }
        ICheckSettings settings = target.fully();
        long start = System.nanoTime();
        eyes.check(settings);
        LOG.info("Visual checkpoint '{}' sent in {} ms{}", name, (System.nanoTime() - start) / 1_000_000,
                ignoredRegions.length > 0 ? " (ignoring " + Arrays.toString(ignoredRegions) + ")" : "");
    }

    /**
     * Ends the Eyes test and returns its results. Differences don't throw here; the caller decides.
     *
     * @return the results, or empty if visual checks are disabled or the test wasn't opened
     */
    public Optional<TestResults> close() {
        if (eyes == null) {
            return Optional.empty();
        }
        try {
            TestResults results = eyes.close(false);
            LOG.info("Eyes test closed: {} checkpoint(s), {} matched, {} different, new baseline: {}",
                    results.getSteps(), results.getMatches(), results.getMismatches(), results.isNew());
            return Optional.of(results);
        } finally {
            eyes = null;
        }
    }

    /** Ends the Eyes test without saving results, e.g. when the scenario failed for another reason. */
    public void abort() {
        if (eyes == null) {
            return;
        }
        try {
            eyes.abortIfNotClosed();
            LOG.info("Eyes test aborted");
        } catch (RuntimeException e) {
            LOG.warn("Could not abort Eyes test: {}", e.getMessage(), e);
        } finally {
            eyes = null;
        }
    }

    /** Called by PicoContainer at the end of the scenario; aborts an Eyes test that is still open. */
    @Override
    public void dispose() {
        abort();
    }

    private RectangleSize viewport() {
        String[] size = config.get(ConfigKeys.APPLITOOLS_VIEWPORT).toLowerCase(Locale.ROOT).split("x");
        return new RectangleSize(Integer.parseInt(size[0].trim()), Integer.parseInt(size[1].trim()));
    }

    private static MatchLevel matchLevel(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "strict" -> MatchLevel.STRICT;
            case "exact" -> MatchLevel.EXACT;
            case "layout" -> MatchLevel.LAYOUT;
            case "content" -> MatchLevel.CONTENT;
            case "ignore_colors" -> MatchLevel.IGNORE_COLORS;
            default -> throw new IllegalArgumentException("Unknown applitools.match-level '" + value
                    + "'. Use strict, exact, layout, content or ignore_colors");
        };
    }
}
