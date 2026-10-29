package com.example.e2e.hooks;

/**
 * Hook order across the hook classes, in one place. For one scenario:
 * <pre>
 *
 * &#64;Before   CONTEXT (ReportingHooks) -> START_BROWSER (BrowserHooks) -> OPEN_EYES (VisualHooks)
 * per step  @BeforeStep / @AfterStep screenshots (ScreenshotHooks)
 * &#64;After    SOFT_ASSERTIONS -> CLOSE_EYES -> FAILURE_SCREENSHOT -> FAILURE_ARTIFACTS -> QUIT_BROWSER -> SCENARIO_LOG
 * </pre>
 * Cucumber runs {@code @Before} hooks from the lowest order to the highest and {@code @After} hooks from
 * the highest to the lowest.
 */
public final class HookOrder {

    /** Stores the scenario in {@code TestContext}; runs first. */
    public static final int CONTEXT = 0;
    /** Starts the browser. */
    public static final int START_BROWSER = 10;
    /** Opens the Applitools Eyes test of a {@code @visual} scenario; needs the running browser. */
    public static final int OPEN_EYES = 20;

    /** Evaluates soft assertions; the first {@code @After} hook, so failures are seen by the hooks after it. */
    public static final int SOFT_ASSERTIONS = 30_000;
    /** Closes the Eyes test; visual differences fail the scenario before the failure artifacts are collected. */
    public static final int CLOSE_EYES = 25_000;
    /** Takes the failure screenshot ({@code screenshot.mode=failures}). */
    public static final int FAILURE_SCREENSHOT = 20_100;
    /** Attaches browser state, page source and console log of failed scenarios. */
    public static final int FAILURE_ARTIFACTS = 20_000;
    /** Quits the browser. */
    public static final int QUIT_BROWSER = 10_000;
    /** After quitting, so the attached log includes the browser shutdown. */
    public static final int SCENARIO_LOG = 1_000;

    private HookOrder() {
    }
}
