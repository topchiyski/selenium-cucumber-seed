package com.example.e2e.steps;

import com.example.e2e.driver.Browser;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

/** Generic, application-independent browser steps. */
public class CommonSteps {

    private final Browser browser;

    /**
     * Creates the steps for one scenario.
     *
     * @param browser the scenario's browser
     */
    public CommonSteps(Browser browser) {
        this.browser = browser;
    }

    /**
     * {@code Given I open the page "/inventory.html"}
     *
     * @param pathOrUrl a path relative to {@code base.url}, or an absolute URL
     */
    @Given("I open the page {string}")
    public void openPage(String pathOrUrl) {
        browser.open(pathOrUrl);
    }

    /**
     * {@code Then the page title should be "Swag Labs"}
     *
     * @param expected the expected title
     */
    @Then("the page title should be {string}")
    public void pageTitleShouldBe(String expected) {
        Assert.assertEquals(browser.title(), expected, "Page title");
    }

    /**
     * {@code Then the current URL should contain "/inventory.html"}; waits up to {@code timeout.explicit}.
     *
     * @param fragment text the URL must contain
     */
    @Then("the current URL should contain {string}")
    public void currentUrlShouldContain(String fragment) {
        try {
            browser.waits().until(ExpectedConditions.urlContains(fragment));
        } catch (TimeoutException e) {
            Assert.fail("Expected URL to contain '" + fragment + "' but was " + browser.currentUrl());
        }
    }
}
