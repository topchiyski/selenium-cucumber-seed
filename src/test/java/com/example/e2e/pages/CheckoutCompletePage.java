package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

/** Order confirmation. */
public class CheckoutCompletePage extends BasePage {

    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public CheckoutCompletePage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return COMPLETE_HEADER;
    }

    /**
     * The confirmation heading.
     *
     * @return e.g. "Thank you for your order!"
     */
    public String confirmationHeader() {
        return text(COMPLETE_HEADER);
    }
}
