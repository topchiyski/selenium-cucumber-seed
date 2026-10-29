package com.example.e2e.pages;

import com.example.e2e.data.Customer;
import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

/** Checkout step one: customer information. */
public class CheckoutInformationPage extends BasePage {

    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL_CODE = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public CheckoutInformationPage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return FIRST_NAME;
    }

    /**
     * Fills in the form and continues; lands on the overview, or stays here with an error.
     *
     * @param customer the details to enter; empty fields are left blank
     */
    public void enterCustomer(Customer customer) {
        log.info("Enter customer details {}", customer);
        type(FIRST_NAME, customer.firstName());
        type(LAST_NAME, customer.lastName());
        type(POSTAL_CODE, customer.postalCode());
        click(CONTINUE);
    }

    /**
     * The validation error shown after continuing with missing fields.
     *
     * @return e.g. "Error: First Name is required"
     */
    public String errorMessage() {
        return text(ERROR);
    }
}
