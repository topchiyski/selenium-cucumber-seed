package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

/** Firefox doesn't expose console logs through WebDriver, so {@link #supportsConsoleLogs()} stays false. */
public class FirefoxSetup implements BrowserSetup {

    @Override
    public AbstractDriverOptions<?> options(BrowserSettings settings) {
        FirefoxOptions options = new FirefoxOptions();
        if (settings.headless()) {
            options.addArguments("-headless",
                    "-width=" + settings.windowSize().getWidth(), "-height=" + settings.windowSize().getHeight());
        }
        options.addArguments(settings.extraArgs());
        return options;
    }

    @Override
    public WebDriver startLocal(AbstractDriverOptions<?> options) {
        return new FirefoxDriver((FirefoxOptions) options);
    }
}
