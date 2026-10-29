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

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public InventoryPage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return INVENTORY;
    }

    /**
     * The page heading.
     *
     * @return "Products"
     */
    public String heading() {
        return text(TITLE);
    }

    /**
     * All product tiles, in display order.
     *
     * @return one card per product
     */
    public List<ProductCard> products() {
        return allVisible(PRODUCT).stream().map(tile -> new ProductCard(tile, waits())).toList();
    }

    /**
     * Finds a product tile by its exact name.
     *
     * @param name the product name, e.g. "Sauce Labs Backpack"
     * @return the product's card
     * @throws java.util.NoSuchElementException if there is no such product (the message lists the available ones)
     */
    public ProductCard product(String name) {
        List<ProductCard> products = products();
        return products.stream()
                .filter(p -> p.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No product named '" + name + "'. Available: "
                        + products.stream().map(ProductCard::name).toList()));
    }

    /**
     * Product names in display order.
     *
     * @return the names
     */
    public List<String> productNames() {
        return products().stream().map(ProductCard::name).toList();
    }

    /**
     * Product prices in display order.
     *
     * @return the prices without currency symbol
     */
    public List<BigDecimal> productPrices() {
        return products().stream().map(ProductCard::price).toList();
    }

    /**
     * {@code az}, {@code za}, {@code lohi} or {@code hilo}.
     *
     * @param option value of the sort dropdown
     */
    public void sortBy(String option) {
        log.info("Sort products by '{}'", option);
        selectByValue(SORT, option);
    }

    /**
     * Adds a product to the cart.
     *
     * @param productName the exact product name
     * @throws java.util.NoSuchElementException if there is no such product
     */
    public void addToCart(String productName) {
        product(productName).addToCart();
    }
}
