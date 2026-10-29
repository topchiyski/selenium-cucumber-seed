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

    public Browser(Config config, WebDriverFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    /** Starts the configured browser, if it isn't running yet. */
    public void start(String sessionName) {
        if (driver != null) {
            return;
        }
        type = BrowserType.from(config.get(ConfigKeys.BROWSER));
        driver = factory.create(type, sessionName);
        waits = new Waits(driver, config.explicitTimeout());
    }

    public boolean isStarted() {
        return driver != null;
    }

    /** The WebDriver session. Fails clearly if a step needs a browser in a scenario that didn't start one. */
    public WebDriver driver() {
        if (driver == null) {
            throw new IllegalStateException("No browser is running for this scenario. "
                    + "Scenarios tagged @api don't start one; remove the tag if this scenario needs a browser.");
        }
        return driver;
    }

    public BrowserType type() {
        return type;
    }

    public Waits waits() {
        driver(); // same clear error as driver() when no browser is running
        return waits;
    }

    /** Opens an absolute URL, or a path relative to {@code base.url}, and waits for the page to load. */
    public void open(String pathOrUrl) {
        String url = resolve(pathOrUrl);
        long start = System.nanoTime();
        driver().get(url);
        waits.untilPageLoaded();
        LOG.info("Opened {} in {} ms", url, (System.nanoTime() - start) / 1_000_000);
    }

    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    public String title() {
        return driver().getTitle();
    }

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
