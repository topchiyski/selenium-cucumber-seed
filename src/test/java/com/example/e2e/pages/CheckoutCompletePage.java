package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

/** Order confirmation. */
public class CheckoutCompletePage extends BasePage {

    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");

    public CheckoutCompletePage(Browser browser) {
        super(browser);
    }

    @Override
    protected By pageMarker() {
        return COMPLETE_HEADER;
    }

    public String confirmationHeader() {
        return text(COMPLETE_HEADER);
    }
}
