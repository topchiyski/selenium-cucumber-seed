package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

import java.util.List;

public class CartPage extends BasePage {

    private static final By CART_LIST = By.cssSelector("[data-test='cart-list']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT = By.id("checkout");

    public CartPage(Browser browser) {
        super(browser);
    }

    @Override
    protected By pageMarker() {
        return CART_LIST;
    }

    public List<String> itemNames() {
        return isDisplayed(ITEM_NAMES) ? texts(ITEM_NAMES) : List.of();
    }

    public void checkout() {
        log.info("Start checkout");
        click(CHECKOUT);
    }
}
