package com.example.e2e.config;

/** Names of the configuration keys used by the framework. See {@code config/default.properties}. */
public final class ConfigKeys {

    public static final String ENV = "env";
    public static final String BASE_URL = "base.url";
    public static final String API_BASE_URL = "api.base.url";
    public static final String BROWSER = "browser";
    public static final String BROWSER_ARGS = "browser.args";
    public static final String HEADLESS = "headless";
    public static final String EXECUTION = "execution";
    public static final String GRID_URL = "grid.url";
    public static final String WINDOW_SIZE = "window.size";
    public static final String PAGE_LOAD_STRATEGY = "page-load.strategy";
    public static final String TIMEOUT_EXPLICIT = "timeout.explicit";
    public static final String TIMEOUT_PAGE_LOAD = "timeout.page-load";
    public static final String TIMEOUT_SCRIPT = "timeout.script";
    public static final String SCREENSHOT_MODE = "screenshot.mode";
    public static final String THREADS = "threads";
    public static final String LOG_ATTACH = "log.attach";

    private ConfigKeys() {
    }
}
