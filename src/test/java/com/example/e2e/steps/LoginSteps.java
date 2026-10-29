package com.example.e2e.steps;

import com.example.e2e.config.Config;
import com.example.e2e.pages.InventoryPage;
import com.example.e2e.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final Config config;
    private final LoginPage loginPage;
    private final InventoryPage inventoryPage;

    public LoginSteps(Config config, LoginPage loginPage, InventoryPage inventoryPage) {
        this.config = config;
        this.loginPage = loginPage;
        this.inventoryPage = inventoryPage;
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
    }

    /** Role names map to {@code user.<role>.username} in the config, so credentials stay out of feature files. */
    @When("I log in as the {string} user")
    public void iLogInAs(String role) {
        loginPage.loginAs(config.get("user." + role + ".username"), config.get("user.password"));
    }

    @Given("I am logged in as the {string} user")
    public void iAmLoggedInAs(String role) {
        iAmOnTheLoginPage();
        iLogInAs(role);
        iShouldSeeTheProductsPage();
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWith(String username, String password) {
        loginPage.loginAs(username, password);
    }

    @Then("I should see the products page")
    public void iShouldSeeTheProductsPage() {
        inventoryPage.waitUntilLoaded();
        Assert.assertEquals(inventoryPage.heading(), "Products", "Products page heading");
    }

    @Then("I should see the login error {string}")
    public void iShouldSeeTheLoginError(String expected) {
        Assert.assertEquals(loginPage.errorMessage(), expected, "Login error message");
    }
}
