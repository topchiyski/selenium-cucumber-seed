package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

import java.math.BigDecimal;

/** Checkout step two: order overview with totals. */
public class CheckoutOverviewPage extends BasePage {

    private static final By ITEM_TOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By FINISH = By.id("finish");

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public CheckoutOverviewPage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return FINISH;
    }

    /**
     * Item total before tax ("Item total: $29.99").
     *
     * @return the item total, e.g. {@code 29.99}
     */
    public BigDecimal itemTotal() {
        return new BigDecimal(text(ITEM_TOTAL).replaceAll("[^0-9.]", ""));
    }

    /** Places the order; leads to {@link CheckoutCompletePage}. */
    public void finish() {
        log.info("Finish checkout");
        click(FINISH);
    }
}
