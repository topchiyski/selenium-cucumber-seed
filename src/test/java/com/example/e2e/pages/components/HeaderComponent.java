package com.example.e2e.pages.components;

import com.example.e2e.driver.Browser;
import com.example.e2e.pages.UiObject;
import org.openqa.selenium.By;

/** The header shown on every page after login: cart link and item-count badge. */
public class HeaderComponent extends UiObject {

    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

    /**
     * Creates the component.
     *
     * @param browser the scenario's browser
     */
    public HeaderComponent(Browser browser) {
        super(browser);
    }

    /**
     * Items in the cart according to the badge (0 when the badge is hidden).
     *
     * @return the number shown on the cart badge
     */
    public int cartCount() {
        return isDisplayed(CART_BADGE) ? Integer.parseInt(text(CART_BADGE)) : 0;
    }

    /** Opens the cart; leads to {@code CartPage}. */
    public void openCart() {
        log.info("Open cart");
        click(CART_LINK);
    }
}
