package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

public class EdgeSetup extends ChromiumSetup<EdgeOptions> {

    @Override
    protected EdgeOptions newOptions() {
        return new EdgeOptions();
    }

    @Override
    protected String loggingPrefsCapability() {
        return "ms:loggingPrefs";
    }

    @Override
    public WebDriver startLocal(AbstractDriverOptions<?> options) {
        return new EdgeDriver((EdgeOptions) options);
    }
}
