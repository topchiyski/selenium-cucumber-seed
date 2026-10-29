package com.example.e2e.pages.components;

import com.example.e2e.support.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * One product tile on the inventory page. Locators are relative to the tile, so the same component works
 * for every product without building selectors from product names.
 */
public class ProductCard {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCard.class);
    private static final By NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By PRICE = By.cssSelector("[data-test='inventory-item-price']");
    private static final By ADD_TO_CART = By.cssSelector("button[data-test^='add-to-cart']");

    private final WebElement root;
    private final Waits waits;

    public ProductCard(WebElement root, Waits waits) {
        this.root = root;
        this.waits = waits;
    }

    public String name() {
        return root.findElement(NAME).getText().trim();
    }

    public BigDecimal price() {
        return new BigDecimal(root.findElement(PRICE).getText().replace("$", "").trim());
    }

    public void addToCart() {
        LOG.info("Add '{}' to cart", name());
        waits.until(ExpectedConditions.elementToBeClickable(root.findElement(ADD_TO_CART))).click();
    }
}
