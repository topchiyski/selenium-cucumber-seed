package com.example.e2e.steps;

import com.example.e2e.data.Customer;
import com.example.e2e.data.TestDataFactory;
import com.example.e2e.pages.CartPage;
import com.example.e2e.pages.CheckoutCompletePage;
import com.example.e2e.pages.CheckoutInformationPage;
import com.example.e2e.pages.CheckoutOverviewPage;
import com.example.e2e.pages.components.HeaderComponent;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.math.BigDecimal;

/** Steps for the checkout flow, from the cart to the order confirmation. */
public class CheckoutSteps {

    private final TestDataFactory testData;
    private final HeaderComponent header;
    private final CartPage cartPage;
    private final CheckoutInformationPage informationPage;
    private final CheckoutOverviewPage overviewPage;
    private final CheckoutCompletePage completePage;

    /**
     * Creates the steps for one scenario.
     *
     * @param testData generates random customers
     * @param header the header, to open the cart
     * @param cartPage the cart
     * @param informationPage checkout step one
     * @param overviewPage checkout step two
     * @param completePage the order confirmation
     */
    public CheckoutSteps(TestDataFactory testData, HeaderComponent header, CartPage cartPage,
                         CheckoutInformationPage informationPage, CheckoutOverviewPage overviewPage,
                         CheckoutCompletePage completePage) {
        this.testData = testData;
        this.header = header;
        this.cartPage = cartPage;
        this.informationPage = informationPage;
        this.overviewPage = overviewPage;
        this.completePage = completePage;
    }

    /** {@code When I check out with a random customer} */
    @When("I check out with a random customer")
    public void iCheckOutWithARandomCustomer() {
        iCheckOutWith(testData.randomCustomer());
    }

    /**
     * The table is converted to a {@link Customer} by {@code DataTableTypes#customer}.
     *
     * @param customer the details to enter
     */
    @When("I check out with the customer:")
    public void iCheckOutWith(Customer customer) {
        header.openCart();
        cartPage.waitUntilLoaded();
        cartPage.checkout();
        informationPage.waitUntilLoaded();
        informationPage.enterCustomer(customer);
    }

    /**
     * {@code Then the item total should be $29.99}
     *
     * @param expected the expected item total before tax
     */
    @Then("the item total should be ${bigdecimal}")
    public void theItemTotalShouldBe(BigDecimal expected) {
        overviewPage.waitUntilLoaded();
        Assert.assertEquals(overviewPage.itemTotal(), expected, "Item total");
    }

    /** {@code When I finish the purchase} */
    @When("I finish the purchase")
    public void iFinishThePurchase() {
        overviewPage.waitUntilLoaded();
        overviewPage.finish();
    }

    /**
     * {@code Then I should see the order confirmation "Thank you for your order!"}
     *
     * @param expected the expected heading
     */
    @Then("I should see the order confirmation {string}")
    public void iShouldSeeTheOrderConfirmation(String expected) {
        completePage.waitUntilLoaded();
        Assert.assertEquals(completePage.confirmationHeader(), expected, "Order confirmation");
    }

    /**
     * {@code Then I should see the checkout error "Error: First Name is required"}
     *
     * @param expected the expected error message
     */
    @Then("I should see the checkout error {string}")
    public void iShouldSeeTheCheckoutError(String expected) {
        Assert.assertEquals(informationPage.errorMessage(), expected, "Checkout error message");
    }
}
