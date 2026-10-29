package com.example.e2e.support;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Explicit waits for one browser session (get it from {@code Browser.waits()}). The implicit wait is
 * zero, so always wait explicitly. Timeouts are logged with the unmet condition before the exception propagates.
 */
public class Waits {

    private static final Logger LOG = LoggerFactory.getLogger(Waits.class);
    /** Waits longer than this are logged at DEBUG; they're usually the first sign of a slow environment. */
    private static final long SLOW_WAIT_MILLIS = 2_000;

    private final WebDriver driver;
    private final Duration defaultTimeout;

    public Waits(WebDriver driver, Duration defaultTimeout) {
        this.driver = driver;
        this.defaultTimeout = defaultTimeout;
    }

    public <T> T until(ExpectedCondition<T> condition) {
        return until(condition, defaultTimeout);
    }

    public <T> T until(ExpectedCondition<T> condition, Duration timeout) {
        long start = System.nanoTime();
        try {
            T result = new WebDriverWait(driver, timeout).until(condition);
            long millis = (System.nanoTime() - start) / 1_000_000;
            if (millis > SLOW_WAIT_MILLIS) {
                LOG.debug("Slow wait: {} ms for {}", millis, condition);
            }
            return result;
        } catch (TimeoutException e) {
            LOG.warn("Timed out after {} s waiting for {} (url: {})", timeout.toSeconds(), condition, safeUrl());
            throw e;
        }
    }

    public void untilPageLoaded() {
        until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"));
            }

            @Override
            public String toString() {
                return "document.readyState == complete";
            }
        });
    }

    private String safeUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException e) {
            return "unavailable";
        }
    }
}
