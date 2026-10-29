package com.example.e2e.hooks;

import com.applitools.eyes.TestResults;
import com.example.e2e.visual.VisualCheck;
import com.example.e2e.visual.VisualTestingRun;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Applitools Eyes lifecycle for scenarios tagged {@code @visual}: opens the Eyes test once the browser runs,
 * and closes it after the steps. Visual differences fail the scenario; the Applitools dashboard link is
 * attached to the Cucumber and Allure reports either way.
 */
public class VisualHooks {

    private static final Logger LOG = LoggerFactory.getLogger(VisualHooks.class);

    private final VisualCheck visual;

    /**
     * Creates the hooks for one scenario.
     *
     * @param visual the scenario's visual checks
     */
    public VisualHooks(VisualCheck visual) {
        this.visual = visual;
    }

    /**
     * Opens the Eyes test, named after the scenario. When visual checks are disabled, notes that in the report.
     *
     * @param scenario the scenario that is about to run
     */
    @Before(value = "@visual", order = HookOrder.OPEN_EYES)
    public void openEyes(Scenario scenario) {
        if (!visual.isEnabled()) {
            scenario.log("Visual checks skipped: set APPLITOOLS_API_KEY to compare screenshots with Applitools baselines.");
        }
        visual.open(scenario.getName()); // no-op when disabled; logs the "skipped" warning once per run
    }

    /**
     * Closes the Eyes test. If the scenario already failed for another reason, the Eyes test is aborted
     * instead, so no partial results are saved.
     *
     * @param scenario the finished scenario
     * @throws AssertionError if Applitools found visual differences
     */
    @After(value = "@visual", order = HookOrder.CLOSE_EYES)
    public void closeEyes(Scenario scenario) {
        if (scenario.isFailed()) {
            visual.abort();
            return;
        }
        Optional<TestResults> closed = visual.close();
        if (closed.isEmpty()) {
            return;
        }
        TestResults results = closed.get();
        scenario.attach(results.getUrl(), "text/uri-list", "Applitools results");
        Allure.link("Applitools: " + scenario.getName(), results.getUrl());
        if (Boolean.TRUE.equals(results.isNew())) {
            LOG.info("New Applitools baseline with {} checkpoint(s); review it at {}", results.getSteps(), results.getUrl());
        } else if (!results.isPassed()) {
            throw new AssertionError("Visual differences in " + results.getMismatches() + " of " + results.getSteps()
                    + " checkpoint(s) (status " + results.getStatus() + "). Review and accept or reject them at "
                    + results.getUrl());
        }
    }

    /** Logs the run's visual results once all scenarios have finished. */
    @AfterAll
    public static void summarize() {
        VisualTestingRun.summarize();
    }
}
