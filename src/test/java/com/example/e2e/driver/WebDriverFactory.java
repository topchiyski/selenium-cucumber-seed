package com.example.e2e.driver;

import com.example.e2e.config.Config;
import com.example.e2e.config.ConfigKeys;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * Creates WebDriver sessions: the browser-specific part comes from the {@link BrowserType}'s
 * {@link BrowserSetup}; local vs remote, timeouts and window size are handled here. Local drivers are
 * resolved automatically by Selenium Manager.
 */
public class WebDriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(WebDriverFactory.class);

    static {
        // The JDK HTTP client tries an HTTP/2 upgrade on plain-HTTP requests; drivers don't support it, and
        // on some machines the new-session request then hangs until the read timeout (3 min). Force 1.1.
        if (System.getProperty("webdriver.httpclient.version") == null) {
            System.setProperty("webdriver.httpclient.version", "HTTP_1_1");
        }
    }

    private final Config config;

    /**
     * Creates a factory.
     *
     * @param config execution mode, grid URL, timeouts and window settings
     */
    public WebDriverFactory(Config config) {
        this.config = config;
    }

    /**
     * Starts a new session, locally or on the grid, with timeouts and window size applied.
     *
     * @param browser the browser to start
     * @param sessionName name shown for the session on a Selenium Grid
     * @return the new session
     * @throws org.openqa.selenium.WebDriverException if the browser can't be started (logged with context first)
     */
    public WebDriver create(BrowserType browser, String sessionName) {
        BrowserSettings settings = BrowserSettings.from(config);
        AbstractDriverOptions<?> options = browser.setup().options(settings);
        options.setPageLoadStrategy(PageLoadStrategy.fromString(config.get(ConfigKeys.PAGE_LOAD_STRATEGY)));

        String target = config.remote() ? "grid " + config.get(ConfigKeys.GRID_URL) : "local";
        LOG.debug("Requested capabilities: {}", options.asMap());
        long start = System.nanoTime();
        WebDriver driver;
        try {
            driver = config.remote() ? startRemote(options, sessionName) : browser.setup().startLocal(options);
        } catch (RuntimeException e) {
            // Logged here as well as rethrown: the scenario report only shows the exception, the log adds the context.
            LOG.error("Could not start {} ({}, headless={}) after {} ms", browser, target, settings.headless(), elapsedMillis(start), e);
            throw e;
        }
        if (driver instanceof RemoteWebDriver session) {
            LOG.info("Started {} {} ({}, headless={}) in {} ms, session {}", browser, session.getCapabilities().getBrowserVersion(),
                    target, settings.headless(), elapsedMillis(start), session.getSessionId());
        }

        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // explicit waits only
        driver.manage().timeouts().pageLoadTimeout(config.getSeconds(ConfigKeys.TIMEOUT_PAGE_LOAD));
        driver.manage().timeouts().scriptTimeout(config.getSeconds(ConfigKeys.TIMEOUT_SCRIPT));
        if (!settings.headless()) {
            driver.manage().window().setSize(settings.windowSize());
        }
        return driver;
    }

    private WebDriver startRemote(AbstractDriverOptions<?> options, String sessionName) {
        options.setCapability("se:name", sessionName); // shown in the Grid UI
        RemoteWebDriver remote = new RemoteWebDriver(gridUrl(), options);
        remote.setFileDetector(new LocalFileDetector()); // lets sendKeys(path) upload local files to the grid
        return remote;
    }

    private URL gridUrl() {
        String url = config.get(ConfigKeys.GRID_URL);
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid grid.url: " + url, e);
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
