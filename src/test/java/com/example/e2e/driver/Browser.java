package com.example.e2e.driver;

import com.example.e2e.config.Config;
import com.example.e2e.config.ConfigKeys;
import com.example.e2e.support.Waits;
import org.openqa.selenium.WebDriver;
import org.picocontainer.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The scenario's browser: owns the WebDriver session and offers navigation and waiting.
 * <p>
 * PicoContainer creates one per scenario and injects it into hooks, page objects and steps. A scenario runs
 * on a single thread, so no ThreadLocal is needed. {@code BrowserHooks} starts and quits it; {@link #dispose()}
 * quits it as a safety net if a scenario ends some other way.
 */
public class Browser implements Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(Browser.class);

    private final Config config;
    private final WebDriverFactory factory;
    private WebDriver driver;
    private BrowserType type;
    private Waits waits;

    /**
     * Creates the scenario's browser; nothing is started until {@link #start(String)}.
     *
     * @param config supplies the browser type, base URL and wait timeout
     * @param factory creates the WebDriver session
     */
    public Browser(Config config, WebDriverFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    /**
     * Starts the configured browser, if it isn't running yet.
     *
     * @param sessionName name shown for the session on a Selenium Grid, usually the scenario name
     */
    public void start(String sessionName) {
        if (driver != null) {
            return;
        }
        type = BrowserType.from(config.get(ConfigKeys.BROWSER));
        driver = factory.create(type, sessionName);
        waits = new Waits(driver, config.explicitTimeout());
    }

    /**
     * Whether a browser session is running.
     *
     * @return {@code true} between {@link #start(String)} and {@link #quit()}
     */
    public boolean isStarted() {
        return driver != null;
    }

    /**
     * The WebDriver session. Fails clearly if a step needs a browser in a scenario that didn't start one.
     *
     * @return the running session
     * @throws IllegalStateException if no browser is running
     */
    public WebDriver driver() {
        if (driver == null) {
            throw new IllegalStateException("No browser is running for this scenario. "
                    + "Scenarios tagged @api don't start one; remove the tag if this scenario needs a browser.");
        }
        return driver;
    }

    /**
     * The browser that was started.
     *
     * @return the browser type, or {@code null} before {@link #start(String)}
     */
    public BrowserType type() {
        return type;
    }

    /**
     * Explicit waits bound to this session, using {@code timeout.explicit} by default.
     *
     * @return the waits
     * @throws IllegalStateException if no browser is running
     */
    public Waits waits() {
        driver(); // same clear error as driver() when no browser is running
        return waits;
    }

    /**
     * Opens an absolute URL, or a path relative to {@code base.url}, and waits for the page to load.
     *
     * @param pathOrUrl e.g. {@code /inventory.html} or {@code https://example.com}
     * @throws IllegalStateException if no browser is running
     */
    public void open(String pathOrUrl) {
        String url = resolve(pathOrUrl);
        long start = System.nanoTime();
        driver().get(url);
        waits.untilPageLoaded();
        LOG.info("Opened {} in {} ms", url, (System.nanoTime() - start) / 1_000_000);
    }

    /**
     * The URL of the current page.
     *
     * @return the current URL
     * @throws IllegalStateException if no browser is running
     */
    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    /**
     * The title of the current page.
     *
     * @return the page title
     * @throws IllegalStateException if no browser is running
     */
    public String title() {
        return driver().getTitle();
    }

    /** Closes the browser session. Safe to call when no browser is running or more than once. */
    public void quit() {
        if (driver == null) {
            return;
        }
        try {
            long start = System.nanoTime();
            driver.quit();
            LOG.info("Closed {} in {} ms", type, (System.nanoTime() - start) / 1_000_000);
        } catch (RuntimeException e) {
            LOG.warn("Failed to quit driver cleanly", e);
        } finally {
            driver = null;
            waits = null;
        }
    }

    /** Called by PicoContainer at the end of the scenario; quits the browser if it's still running. */
    @Override
    public void dispose() {
        quit();
    }

    private String resolve(String pathOrUrl) {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }
        String base = config.baseUrl().replaceAll("/+$", "");
        if (pathOrUrl.isEmpty() || pathOrUrl.equals("/")) {
            return base;
        }
        return base + (pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl);
    }
}
