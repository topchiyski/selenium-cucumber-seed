package com.example.e2e.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Re-runs only the scenarios that failed in the last {@link CucumberTestRunner} run
 * ({@code ./gradlew rerunFailed}). Results are added to the same Allure results directory, so the
 * report shows the rerun as a retry of the original failure.
 */
@CucumberOptions(
        features = "@build/rerun/failed.txt",
        glue = "com.example.e2e",
        plugin = {
                "pretty",
                "summary",
                "html:build/reports/cucumber-rerun/cucumber.html",
                "json:build/reports/cucumber-rerun/cucumber.json",
                "rerun:build/rerun/still-failing.txt",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.example.e2e.support.StepTracker",
                "com.example.e2e.logging.ExecutionLogger"
        })
public class RerunFailedRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
