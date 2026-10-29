package com.example.e2e.steps;

import com.example.e2e.context.TestContext;
import com.example.e2e.pages.CartPage;
import com.example.e2e.pages.InventoryPage;
import com.example.e2e.pages.components.HeaderComponent;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Steps for the product list and the cart. */
public class CartSteps {

    private final TestContext context;
    private final InventoryPage inventoryPage;
    private final CartPage cartPage;
    private final HeaderComponent header;

    /**
     * Creates the steps for one scenario.
     *
     * @param context the scenario context, for soft assertions
     * @param inventoryPage the product list
     * @param cartPage the cart
     * @param header the header with the cart link and badge
     */
    public CartSteps(TestContext context, InventoryPage inventoryPage, CartPage cartPage, HeaderComponent header) {
        this.context = context;
        this.inventoryPage = inventoryPage;
        this.cartPage = cartPage;
        this.header = header;
    }

    /**
     * {@code Given I have added "Sauce Labs Backpack" to the cart}
     *
     * @param product the exact product name
     */
    @Given("I have added {string} to the cart")
    @When("I add {string} to the cart")
    public void iAddToTheCart(String product) {
        inventoryPage.addToCart(product);
    }

    /**
     * {@code When I add the following products to the cart:} followed by a one-column table.
     *
     * @param products the product names
     */
    @When("I add the following products to the cart:")
    public void iAddProducts(List<String> products) {
        products.forEach(inventoryPage::addToCart);
    }

    /**
     * {@code Then the cart badge should show 2}
     *
     * @param expected the expected count
     */
    @Then("the cart badge should show {int}")
    public void theCartBadgeShouldShow(int expected) {
        Assert.assertEquals(header.cartCount(), expected, "Cart badge count");
    }

    /** {@code When I open the cart}: opens the cart and waits until it is shown. */
    @When("I open the cart")
    public void iOpenTheCart() {
        header.openCart();
        cartPage.waitUntilLoaded();
    }

    /**
     * {@code Then the cart should contain:} followed by a one-column table, in display order.
     *
     * @param expected the expected product names
     */
    @Then("the cart should contain:")
    public void theCartShouldContain(List<String> expected) {
        Assert.assertEquals(cartPage.itemNames(), expected, "Cart items");
    }

    /**
     * {@code When I sort the products by price (low to high)}
     *
     * @param sortValue dropdown value, converted by {@code DataTableTypes#sortOrder}
     */
    @When("I sort the products by {sortOrder}")
    public void iSortTheProductsBy(String sortValue) {
        inventoryPage.sortBy(sortValue);
    }

    /**
     * {@code Then the product prices should be in ascending order}
     *
     * @param direction {@code ascending} or {@code descending}
     */
    @Then("the product prices should be in {word} order")
    public void productPricesShouldBeSorted(String direction) {
        assertSorted(inventoryPage.productPrices(), Comparator.naturalOrder(), direction, "Product prices");
    }

    /**
     * {@code Then the product names should be in descending order}
     *
     * @param direction {@code ascending} or {@code descending}
     */
    @Then("the product names should be in {word} order")
    public void productNamesShouldBeSorted(String direction) {
        assertSorted(inventoryPage.productNames(), Comparator.naturalOrder(), direction, "Product names");
    }

    /** Example of soft assertions: every check runs, and all failures are reported together after the scenario. */
    @Then("the product catalogue should be complete")
    public void theProductCatalogueShouldBeComplete() {
        List<String> names = inventoryPage.productNames();
        List<BigDecimal> prices = inventoryPage.productPrices();
        context.softly().assertEquals(names.size(), 6, "Number of products");
        context.softly().assertEquals(prices.size(), names.size(), "Every product has a price");
        prices.forEach(p -> context.softly().assertTrue(p.signum() > 0, "Price should be positive but was " + p));
        names.forEach(n -> context.softly().assertTrue(n.startsWith("Sauce Labs") || n.startsWith("Test.allTheThings()"),
                "Unexpected product name: " + n));
    }

    private static <T> void assertSorted(List<T> actual, Comparator<T> ascending, String direction, String what) {
        Comparator<T> order = switch (direction) {
            case "ascending" -> ascending;
            case "descending" -> ascending.reversed();
            default -> throw new IllegalArgumentException("Use 'ascending' or 'descending', not '" + direction + "'");
        };
        Assert.assertEquals(actual, actual.stream().sorted(order).toList(), what + " in " + direction + " order");
    }
}
