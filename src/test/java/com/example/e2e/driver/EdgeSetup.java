package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

/** Microsoft Edge setup. */
public class EdgeSetup extends ChromiumSetup<EdgeOptions> {

    /** Creates the setup; one instance is shared through {@link BrowserType#EDGE}. */
    public EdgeSetup() {
    }

    /** {@inheritDoc} */
    @Override
    protected EdgeOptions newOptions() {
        return new EdgeOptions();
    }

    /** {@inheritDoc} */
    @Override
    protected String loggingPrefsCapability() {
        return "ms:loggingPrefs";
    }

    /** {@inheritDoc} */
    @Override
    public WebDriver startLocal(AbstractDriverOptions<?> options) {
        return new EdgeDriver((EdgeOptions) options);
    }
}
