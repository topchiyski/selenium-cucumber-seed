package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import com.example.e2e.pages.components.ProductCard;
import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

/** The product list. The cart link and badge are in {@link com.example.e2e.pages.components.HeaderComponent}. */
public class InventoryPage extends BasePage {

    private static final By INVENTORY = By.cssSelector("[data-test='inventory-container']");
    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By PRODUCT = By.cssSelector("[data-test='inventory-item']");
    private static final By SORT = By.cssSelector("[data-test='product-sort-container']");

    public InventoryPage(Browser browser) {
        super(browser);
    }

    @Override
    protected By pageMarker() {
        return INVENTORY;
    }

    public String heading() {
        return text(TITLE);
    }

    public List<ProductCard> products() {
        return allVisible(PRODUCT).stream().map(tile -> new ProductCard(tile, waits())).toList();
    }

    public ProductCard product(String name) {
        List<ProductCard> products = products();
        return products.stream()
                .filter(p -> p.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No product named '" + name + "'. Available: "
                        + products.stream().map(ProductCard::name).toList()));
    }

    public List<String> productNames() {
        return products().stream().map(ProductCard::name).toList();
    }

    public List<BigDecimal> productPrices() {
        return products().stream().map(ProductCard::price).toList();
    }

    /** {@code az}, {@code za}, {@code lohi} or {@code hilo}. */
    public void sortBy(String option) {
        log.info("Sort products by '{}'", option);
        selectByValue(SORT, option);
    }

    public void addToCart(String productName) {
        product(productName).addToCart();
    }
}
