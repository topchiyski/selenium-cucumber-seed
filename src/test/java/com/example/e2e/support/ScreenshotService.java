package com.example.e2e.support;

import com.example.e2e.driver.Browser;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/** Takes screenshots of the scenario's browser and attaches them to the Cucumber/Allure reports. */
public class ScreenshotService {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotService.class);

    private final Browser browser;

    /**
     * Creates the service.
     *
     * @param browser the scenario's browser
     */
    public ScreenshotService(Browser browser) {
        this.browser = browser;
    }

    /**
     * Takes a PNG screenshot, or returns empty if no browser is running or the browser can't respond.
     *
     * @return the PNG bytes, or empty
     */
    public Optional<byte[]> capture() {
        if (!browser.isStarted()) {
            return Optional.empty();
        }
        try {
            long start = System.nanoTime();
            byte[] png = ((TakesScreenshot) browser.driver()).getScreenshotAs(OutputType.BYTES);
            LOG.trace("Screenshot taken: {} KB in {} ms", png.length / 1024, (System.nanoTime() - start) / 1_000_000);
            return Optional.of(png);
        } catch (RuntimeException e) {
            // e.g. an alert is open or the session died; never fail a step because of a screenshot
            LOG.warn("Could not take screenshot: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Attaches a screenshot to the scenario; shows up in both the Cucumber HTML and the Allure report.
     *
     * @param scenario the scenario to attach to
     * @param name the attachment name
     */
    public void attach(Scenario scenario, String name) {
        capture().ifPresent(png -> {
            scenario.attach(png, "image/png", name);
            LOG.debug("Attached screenshot '{}'", name);
        });
    }
}
