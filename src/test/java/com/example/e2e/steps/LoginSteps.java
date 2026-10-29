package com.example.e2e.steps;

import com.example.e2e.config.Config;
import com.example.e2e.pages.InventoryPage;
import com.example.e2e.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/** Steps for logging in. */
public class LoginSteps {

    private final Config config;
    private final LoginPage loginPage;
    private final InventoryPage inventoryPage;

    /**
     * Creates the steps for one scenario.
     *
     * @param config supplies the credentials for user roles
     * @param loginPage the login page
     * @param inventoryPage the page shown after a successful login
     */
    public LoginSteps(Config config, LoginPage loginPage, InventoryPage inventoryPage) {
        this.config = config;
        this.loginPage = loginPage;
        this.inventoryPage = inventoryPage;
    }

    /** {@code Given I am on the login page} */
    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
    }

    /**
     * Role names map to {@code user.<role>.username} in the config, so credentials stay out of feature files.
     *
     * @param role user role, e.g. {@code standard}
     */
    @When("I log in as the {string} user")
    public void iLogInAs(String role) {
        loginPage.loginAs(config.get("user." + role + ".username"), config.get("user.password"));
    }

    /**
     * {@code Given I am logged in as the "standard" user}: opens the login page, logs in and waits for the products page.
     *
     * @param role user role, e.g. {@code standard}
     */
    @Given("I am logged in as the {string} user")
    public void iAmLoggedInAs(String role) {
        iAmOnTheLoginPage();
        iLogInAs(role);
        iShouldSeeTheProductsPage();
    }

    /**
     * {@code When I log in with username "..." and password "..."}, for negative login tests.
     *
     * @param username the user name to enter
     * @param password the password to enter
     */
    @When("I log in with username {string} and password {string}")
    public void iLogInWith(String username, String password) {
        loginPage.loginAs(username, password);
    }

    /** {@code Then I should see the products page} */
    @Then("I should see the products page")
    public void iShouldSeeTheProductsPage() {
        inventoryPage.waitUntilLoaded();
        Assert.assertEquals(inventoryPage.heading(), "Products", "Products page heading");
    }

    /**
     * {@code Then I should see the login error "..."}
     *
     * @param expected the expected error message
     */
    @Then("I should see the login error {string}")
    public void iShouldSeeTheLoginError(String expected) {
        Assert.assertEquals(loginPage.errorMessage(), expected, "Login error message");
    }
}
