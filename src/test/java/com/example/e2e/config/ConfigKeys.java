package com.example.e2e.config;

/** Names of the configuration keys used by the framework. See {@code config/default.properties}. */
public final class ConfigKeys {

    /** Environment name selecting {@code config/<env>.properties}. */
    public static final String ENV = "env";
    /** Base URL of the application under test. */
    public static final String BASE_URL = "base.url";
    /** Base URL for API requests. */
    public static final String API_BASE_URL = "api.base.url";
    /** Browser to run: {@code chrome}, {@code firefox} or {@code edge}. */
    public static final String BROWSER = "browser";
    /** Extra browser command-line arguments, comma separated. */
    public static final String BROWSER_ARGS = "browser.args";
    /** Run browsers without a window. */
    public static final String HEADLESS = "headless";
    /** {@code local} or {@code remote} (Selenium Grid). */
    public static final String EXECUTION = "execution";
    /** Selenium Grid URL for {@code execution=remote}. */
    public static final String GRID_URL = "grid.url";
    /** Browser window size, {@code <width>x<height>}. */
    public static final String WINDOW_SIZE = "window.size";
    /** WebDriver page-load strategy: {@code normal}, {@code eager} or {@code none}. */
    public static final String PAGE_LOAD_STRATEGY = "page-load.strategy";
    /** Default explicit-wait timeout in seconds. */
    public static final String TIMEOUT_EXPLICIT = "timeout.explicit";
    /** Page-load timeout in seconds. */
    public static final String TIMEOUT_PAGE_LOAD = "timeout.page-load";
    /** Script timeout in seconds. */
    public static final String TIMEOUT_SCRIPT = "timeout.script";
    /** Screenshot mode: {@code all}, {@code failures} or {@code none}. */
    public static final String SCREENSHOT_MODE = "screenshot.mode";
    /** Number of scenarios run in parallel. */
    public static final String THREADS = "threads";
    /** When to attach the scenario log: {@code all}, {@code failures} or {@code none}. */
    public static final String LOG_ATTACH = "log.attach";
    /** Turns Applitools visual checks off even when an API key is present. */
    public static final String APPLITOOLS_ENABLED = "applitools.enabled";
    /** Applitools API key; set it as the {@code APPLITOOLS_API_KEY} environment variable, never in a file. */
    public static final String APPLITOOLS_API_KEY = "applitools.api-key";
    /** Application name that groups baselines in the Applitools dashboard. */
    public static final String APPLITOOLS_APP_NAME = "applitools.app-name";
    /** Name of the Applitools batch that collects one run's results. */
    public static final String APPLITOOLS_BATCH_NAME = "applitools.batch-name";
    /** Optional batch id ({@code APPLITOOLS_BATCH_ID}), so several JVMs or CI jobs report into one batch. */
    public static final String APPLITOOLS_BATCH_ID = "applitools.batch-id";
    /** Default match level: {@code strict}, {@code exact}, {@code layout}, {@code content} or {@code ignore_colors}. */
    public static final String APPLITOOLS_MATCH_LEVEL = "applitools.match-level";
    /** Browser viewport for visual checks, {@code <width>x<height>}; part of each baseline's identity. */
    public static final String APPLITOOLS_VIEWPORT = "applitools.viewport";
    /** Optional server URL for a dedicated Applitools cloud. */
    public static final String APPLITOOLS_SERVER_URL = "applitools.server-url";

    private ConfigKeys() {
    }
}
