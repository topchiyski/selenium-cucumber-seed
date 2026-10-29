package com.example.e2e.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import java.util.List;

public class ChromeSetup extends ChromiumSetup<ChromeOptions> {

    @Override
    protected ChromeOptions newOptions() {
        return new ChromeOptions();
    }

    @Override
    protected String loggingPrefsCapability() {
        return "goog:loggingPrefs";
    }

    @Override
    protected List<String> browserArgs() {
        return List.of("--disable-search-engine-choice-screen");
    }

    @Override
    public WebDriver startLocal(AbstractDriverOptions<?> options) {
        return new ChromeDriver((ChromeOptions) options);
    }
}
