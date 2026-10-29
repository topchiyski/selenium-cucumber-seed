package com.example.e2e.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Main entry point: runs every feature under {@code src/test/resources/features}, one TestNG test per
 * scenario, in parallel. Override at runtime with -Dcucumber.filter.tags / -Dcucumber.features.
 */
@CucumberOptions(
        features = "classpath:features",
        glue = "com.example.e2e",
        tags = "not @wip and not @known-failure",
        plugin = {
                "pretty",
                "summary",
                "html:build/reports/cucumber/cucumber.html",
                "json:build/reports/cucumber/cucumber.json",
                "rerun:build/rerun/failed.txt",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.example.e2e.support.StepTracker",
                "com.example.e2e.logging.ExecutionLogger"
        })
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    /** Created by TestNG from testng.xml. */
    public CucumberTestRunner() {
    }

    /**
     * Exposes the scenarios as a parallel DataProvider; the thread count comes from {@link ParallelismListener}.
     *
     * @return one row per scenario
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
