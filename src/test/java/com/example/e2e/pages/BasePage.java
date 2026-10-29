package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/**
 * Base class for page objects: one class per screen. Page objects are created by PicoContainer (add them
 * to a step definition's constructor) and get the scenario's {@link Browser}.
 * <p>
 * Each page names an element that only it shows ({@link #pageMarker()}). Steps call
 * {@link #waitUntilLoaded()} on the page they expect to land on, so a wrong navigation fails right there
 * with a clear message instead of later with "element not found".
 */
public abstract class BasePage extends UiObject {

    protected BasePage(Browser browser) {
        super(browser);
    }

    /** An element that is visible only on this page. */
    protected abstract By pageMarker();

    /** Waits for this page; fails with the expected page and the actual URL if it doesn't appear. */
    public void waitUntilLoaded() {
        try {
            visible(pageMarker());
            log.debug("{} is loaded", getClass().getSimpleName());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Expected to be on " + getClass().getSimpleName() + " (waited for " + pageMarker()
                    + ") but the browser is at " + browser.currentUrl(), e);
        }
    }

    /** True if this page is shown right now (doesn't wait). */
    public boolean isLoaded() {
        return isDisplayed(pageMarker());
    }

    /** Opens a path relative to {@code base.url} and waits for this page. */
    protected void open(String path) {
        browser.open(path);
        waitUntilLoaded();
    }
}
