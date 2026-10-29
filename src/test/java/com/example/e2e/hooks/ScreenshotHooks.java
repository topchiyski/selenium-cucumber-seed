package com.example.e2e.hooks;

import com.example.e2e.config.Config;
import com.example.e2e.config.Config.AttachMode;
import com.example.e2e.support.ScreenshotService;
import com.example.e2e.support.StepTracker;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

import java.util.Locale;

/** Screenshots before and after every step ({@code screenshot.mode=all}) or once on failure ({@code failures}). */
public class ScreenshotHooks {

    private final Config config;
    private final ScreenshotService screenshots;
    private int stepIndex;

    /**
     * Creates the hooks for one scenario.
     *
     * @param config supplies {@code screenshot.mode}
     * @param screenshots takes the screenshots
     */
    public ScreenshotHooks(Config config, ScreenshotService screenshots) {
        this.config = config;
        this.screenshots = screenshots;
    }

    /**
     * Attaches a screenshot taken before the step runs.
     *
     * @param scenario the running scenario
     */
    @BeforeStep
    public void beforeStep(Scenario scenario) {
        stepScreenshot(scenario, "Before");
    }

    /**
     * Attaches a screenshot taken after the step ran (also after a failing step).
     *
     * @param scenario the running scenario
     */
    @AfterStep
    public void afterStep(Scenario scenario) {
        stepScreenshot(scenario, "After");
        stepIndex++;
    }

    /**
     * In {@code all} mode the after-step screenshot already shows the failing state.
     *
     * @param scenario the finished scenario
     */
    @After(order = HookOrder.FAILURE_SCREENSHOT)
    public void failureScreenshot(Scenario scenario) {
        if (scenario.isFailed() && config.screenshotMode() == AttachMode.FAILURES) {
            screenshots.attach(scenario, "Failure screenshot");
        }
    }

    private void stepScreenshot(Scenario scenario, String when) {
        if (config.screenshotMode() != AttachMode.ALL) {
            return;
        }
        String stepText = StepTracker.stepText(stepIndex);
        screenshots.capture().ifPresent(png -> {
            // Allure shows each step hook as its own step; give it a readable name instead of the method signature.
            Allure.getLifecycle().updateStep(step -> step.setName("Screenshot " + when.toLowerCase(Locale.ROOT) + ": " + stepText));
            scenario.attach(png, "image/png", when + ": " + stepText);
        });
    }
}
