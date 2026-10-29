package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import com.example.e2e.logging.Masking;
import com.example.e2e.support.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wait-aware interaction helpers shared by pages ({@link BasePage}) and page components (e.g. a header).
 * Every interaction waits explicitly for the element and is logged at DEBUG under the concrete class.
 * Use {@link #typeSecret} for passwords so the value never reaches the logs.
 */
public abstract class UiObject {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Browser browser;

    protected UiObject(Browser browser) {
        this.browser = browser;
    }

    protected WebDriver driver() {
        return browser.driver();
    }

    protected Waits waits() {
        return browser.waits();
    }

    protected WebElement visible(By locator) {
        return waits().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> allVisible(By locator) {
        return waits().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void click(By locator) {
        log.debug("click {}", locator);
        waits().until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        log.debug("type '{}' into {}", text, locator);
        enter(locator, text);
    }

    /** Like {@link #type} but never logs the value. */
    protected void typeSecret(By locator, String text) {
        log.debug("type {} into {}", Masking.MASK, locator);
        enter(locator, text);
    }

    private void enter(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String text(By locator) {
        String text = visible(locator).getText().trim();
        log.debug("text of {} = '{}'", locator, text);
        return text;
    }

    protected List<String> texts(By locator) {
        List<String> texts = allVisible(locator).stream().map(e -> e.getText().trim()).toList();
        log.debug("texts of {} = {}", locator, texts);
        return texts;
    }

    protected void selectByValue(By locator, String value) {
        log.debug("select value '{}' in {}", value, locator);
        new Select(visible(locator)).selectByValue(value);
    }

    /** True if the element is present and visible right now (doesn't wait). */
    protected boolean isDisplayed(By locator) {
        boolean displayed = driver().findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        log.debug("{} displayed = {}", locator, displayed);
        return displayed;
    }
}
