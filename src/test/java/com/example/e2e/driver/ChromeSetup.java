package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import java.util.List;

/** Google Chrome setup. */
public class ChromeSetup extends ChromiumSetup<ChromeOptions> {

    /** Creates the setup; one instance is shared through {@link BrowserType#CHROME}. */
    public ChromeSetup() {
    }

    /** {@inheritDoc} */
    @Override
    protected ChromeOptions newOptions() {
        return new ChromeOptions();
    }

    /** {@inheritDoc} */
    @Override
    protected String loggingPrefsCapability() {
        return "goog:loggingPrefs";
    }

    /**
     * Suppresses Chrome's search-engine choice dialog, which would block the first page.
     *
     * @return the Chrome-only arguments
     */
    @Override
    protected List<String> browserArgs() {
        return List.of("--disable-search-engine-choice-screen");
    }

    /** {@inheritDoc} */
    @Override
    public WebDriver startLocal(AbstractDriverOptions<?> options) {
        return new ChromeDriver((ChromeOptions) options);
    }
}
