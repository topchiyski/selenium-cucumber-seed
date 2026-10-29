package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

import java.util.List;

/** The shopping cart. */
public class CartPage extends BasePage {

    private static final By CART_LIST = By.cssSelector("[data-test='cart-list']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT = By.id("checkout");

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public CartPage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return CART_LIST;
    }

    /**
     * Names of the products in the cart, in display order.
     *
     * @return the product names; empty if the cart is empty
     */
    public List<String> itemNames() {
        return isDisplayed(ITEM_NAMES) ? texts(ITEM_NAMES) : List.of();
    }

    /** Starts checkout; leads to {@link CheckoutInformationPage}. */
    public void checkout() {
        log.info("Start checkout");
        click(CHECKOUT);
    }
}
