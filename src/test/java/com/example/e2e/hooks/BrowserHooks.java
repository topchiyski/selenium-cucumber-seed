package com.example.e2e.hooks;

import com.example.e2e.driver.Browser;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** Starts the scenario's browser (except for {@code @api} scenarios) and quits it afterwards. */
public class BrowserHooks {

    private final Browser browser;

    public BrowserHooks(Browser browser) {
        this.browser = browser;
    }

    @Before(value = "not @api", order = HookOrder.START_BROWSER)
    public void startBrowser(Scenario scenario) {
        browser.start(scenario.getName());
    }

    @After(order = HookOrder.QUIT_BROWSER)
    public void quitBrowser() {
        browser.quit();
    }
}
