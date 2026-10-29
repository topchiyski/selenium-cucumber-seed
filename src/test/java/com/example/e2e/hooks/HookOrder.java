package com.example.e2e.hooks;

/**
 * Hook order across the hook classes, in one place. For one scenario:
 * <pre>
 * @Before   CONTEXT (ReportingHooks) -> START_BROWSER (BrowserHooks)
 * per step  @BeforeStep / @AfterStep screenshots (ScreenshotHooks)
 * @After    SOFT_ASSERTIONS -> FAILURE_SCREENSHOT -> FAILURE_ARTIFACTS -> QUIT_BROWSER -> SCENARIO_LOG
 * </pre>
 * Cucumber runs {@code @Before} hooks from the lowest order to the highest and {@code @After} hooks from
 * the highest to the lowest.
 */
public final class HookOrder {

    public static final int CONTEXT = 0;
    public static final int START_BROWSER = 10;

    public static final int SOFT_ASSERTIONS = 30_000;
    public static final int FAILURE_SCREENSHOT = 20_100;
    public static final int FAILURE_ARTIFACTS = 20_000;
    public static final int QUIT_BROWSER = 10_000;
    /** After quitting, so the attached log includes the browser shutdown. */
    public static final int SCENARIO_LOG = 1_000;

    private HookOrder() {
    }
}
