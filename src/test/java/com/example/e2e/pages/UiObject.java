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

    /** Logger named after the concrete page or component class. */
    protected final Logger log = LoggerFactory.getLogger(getClass());
    /** The scenario's browser. */
    protected final Browser browser;

    /**
     * Creates the UI object.
     *
     * @param browser the scenario's browser
     */
    protected UiObject(Browser browser) {
        this.browser = browser;
    }

    /**
     * The WebDriver session, for interactions the helpers don't cover.
     *
     * @return the session
     */
    protected WebDriver driver() {
        return browser.driver();
    }

    /**
     * Explicit waits for custom conditions.
     *
     * @return the browser's waits
     */
    protected Waits waits() {
        return browser.waits();
    }

    /**
     * Waits until an element is visible.
     *
     * @param locator the element
     * @return the element
     * @throws org.openqa.selenium.TimeoutException if it doesn't become visible in time
     */
    protected WebElement visible(By locator) {
        return waits().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until all elements matching the locator are visible.
     *
     * @param locator the elements
     * @return the elements, in document order
     * @throws org.openqa.selenium.TimeoutException if none become visible in time
     */
    protected List<WebElement> allVisible(By locator) {
        return waits().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until an element is clickable, then clicks it.
     *
     * @param locator the element
     * @throws org.openqa.selenium.TimeoutException if it doesn't become clickable in time
     */
    protected void click(By locator) {
        log.debug("click {}", locator);
        waits().until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Waits until a field is visible, clears it and types the text (logged at DEBUG).
     *
     * @param locator the input field
     * @param text the text to type
     */
    protected void type(By locator, String text) {
        log.debug("type '{}' into {}", text, locator);
        enter(locator, text);
    }

    /**
     * Like {@link #type} but never logs the value.
     *
     * @param locator the input field
     * @param text the secret to type
     */
    protected void typeSecret(By locator, String text) {
        log.debug("type {} into {}", Masking.MASK, locator);
        enter(locator, text);
    }

    private void enter(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Waits until an element is visible and returns its text.
     *
     * @param locator the element
     * @return the visible text, trimmed
     */
    protected String text(By locator) {
        String text = visible(locator).getText().trim();
        log.debug("text of {} = '{}'", locator, text);
        return text;
    }

    /**
     * Waits until the elements are visible and returns their texts.
     *
     * @param locator the elements
     * @return the visible texts, trimmed, in document order
     */
    protected List<String> texts(By locator) {
        List<String> texts = allVisible(locator).stream().map(e -> e.getText().trim()).toList();
        log.debug("texts of {} = {}", locator, texts);
        return texts;
    }

    /**
     * Selects an option of a {@code <select>} element by its {@code value} attribute.
     *
     * @param locator the select element
     * @param value the option value
     */
    protected void selectByValue(By locator, String value) {
        log.debug("select value '{}' in {}", value, locator);
        new Select(visible(locator)).selectByValue(value);
    }

    /**
     * True if the element is present and visible right now (doesn't wait).
     *
     * @param locator the element
     * @return {@code true} if at least one matching element is visible
     */
    protected boolean isDisplayed(By locator) {
        boolean displayed = driver().findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        log.debug("{} displayed = {}", locator, displayed);
        return displayed;
    }
}
