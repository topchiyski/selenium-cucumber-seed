package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

/** The login screen (the site's start page). */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    /**
     * Creates the page object.
     *
     * @param browser the scenario's browser
     */
    public LoginPage(Browser browser) {
        super(browser);
    }

    /** {@inheritDoc} */
    @Override
    protected By pageMarker() {
        return LOGIN_BUTTON;
    }

    /** Opens the login page and waits until it is shown. */
    public void open() {
        open("/");
    }

    /**
     * Submits the login form. The password is never logged.
     *
     * @param username the user name
     * @param password the password
     */
    public void loginAs(String username, String password) {
        log.info("Log in as '{}'", username);
        type(USERNAME, username);
        typeSecret(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    /**
     * The error shown after a rejected login.
     *
     * @return e.g. "Epic sadface: Username is required"
     */
    public String errorMessage() {
        return text(ERROR);
    }
}
