package com.example.e2e.hooks;

import com.example.e2e.driver.Browser;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** Starts the scenario's browser (except for {@code @api} scenarios) and quits it afterwards. */
public class BrowserHooks {

    private final Browser browser;

    /**
     * Creates the hooks for one scenario.
     *
     * @param browser the scenario's browser
     */
    public BrowserHooks(Browser browser) {
        this.browser = browser;
    }

    /**
     * Starts the browser before every scenario not tagged {@code @api}.
     *
     * @param scenario the scenario; its name becomes the grid session name
     */
    @Before(value = "not @api", order = HookOrder.START_BROWSER)
    public void startBrowser(Scenario scenario) {
        browser.start(scenario.getName());
    }

    /** Quits the browser after the failure artifacts have been collected. */
    @After(order = HookOrder.QUIT_BROWSER)
    public void quitBrowser() {
        browser.quit();
    }
}
