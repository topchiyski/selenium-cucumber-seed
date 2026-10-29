package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

import java.math.BigDecimal;

/** Checkout step two: order overview with totals. */
public class CheckoutOverviewPage extends BasePage {

    private static final By ITEM_TOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By FINISH = By.id("finish");

    public CheckoutOverviewPage(Browser browser) {
        super(browser);
    }

    @Override
    protected By pageMarker() {
        return FINISH;
    }

    /** Item total before tax ("Item total: $29.99"). */
    public BigDecimal itemTotal() {
        return new BigDecimal(text(ITEM_TOTAL).replaceAll("[^0-9.]", ""));
    }

    public void finish() {
        log.info("Finish checkout");
        click(FINISH);
    }
}
