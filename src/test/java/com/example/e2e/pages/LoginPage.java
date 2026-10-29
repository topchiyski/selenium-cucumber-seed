package com.example.e2e.pages;

import com.example.e2e.driver.Browser;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(Browser browser) {
        super(browser);
    }

    @Override
    protected By pageMarker() {
        return LOGIN_BUTTON;
    }

    public void open() {
        open("/");
    }

    public void loginAs(String username, String password) {
        log.info("Log in as '{}'", username);
        type(USERNAME, username);
        typeSecret(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    public String errorMessage() {
        return text(ERROR);
    }
}
