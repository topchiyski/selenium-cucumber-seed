package com.example.e2e.hooks;

import com.example.e2e.config.Config;
import com.example.e2e.config.Config.AttachMode;
import com.example.e2e.context.TestContext;
import com.example.e2e.driver.Browser;
import com.example.e2e.logging.ScenarioLogAppender;
import com.example.e2e.support.AllureEnvironmentWriter;
import com.example.e2e.support.BrowserConsole;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Report contents beyond screenshots: run environment, soft-assertion results, failure artifacts and the
 * scenario's log. Scenario/step start and end are logged by {@code ExecutionLogger}, not here.
 */
public class ReportingHooks {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingHooks.class);

    private final TestContext context;
    private final Browser browser;
    private final BrowserConsole console;
    private final Config config;

    public ReportingHooks(TestContext context, Browser browser, BrowserConsole console, Config config) {
        this.context = context;
        this.browser = browser;
        this.console = console;
        this.config = config;
    }

    /** Static, so it runs once per run; PicoContainer isn't involved, hence the shared Config. */
    @BeforeAll
    public static void writeAllureEnvironment() {
        AllureEnvironmentWriter.write(Config.get());
    }

    @Before(order = HookOrder.CONTEXT)
    public void rememberScenario(Scenario scenario) {
        context.setScenario(scenario);
    }

    @After(order = HookOrder.SOFT_ASSERTIONS)
    public void verifySoftAssertions() {
        context.softly().assertAll();
    }

    @After(order = HookOrder.FAILURE_ARTIFACTS)
    public void attachFailureArtifacts(Scenario scenario) {
        if (!scenario.isFailed() || !browser.isStarted()) {
            return;
        }
        LOG.debug("Collecting failure artifacts");
        try {
            StringBuilder info = new StringBuilder("URL: ").append(browser.currentUrl())
                    .append("\nTitle: ").append(browser.title());
            if (browser.driver() instanceof RemoteWebDriver remote) {
                info.append("\nSession id: ").append(remote.getSessionId());
            }
            LOG.info("Browser state at failure: {}", info.toString().replace('\n', ' '));
            scenario.attach(info.toString(), "text/plain", "Browser state");
            scenario.attach(browser.driver().getPageSource().getBytes(StandardCharsets.UTF_8), "text/html", "Page source");
        } catch (RuntimeException e) {
            LOG.warn("Could not collect browser state: {}", e.getMessage(), e);
        }
        console.collect().ifPresent(logs -> scenario.attach(logs, "text/plain", "Browser console log"));
    }

    @After(order = HookOrder.SCENARIO_LOG)
    public void attachScenarioLog(Scenario scenario) {
        AttachMode mode = config.scenarioLogMode();
        if (mode == AttachMode.ALL || (mode == AttachMode.FAILURES && scenario.isFailed())) {
            String log = ScenarioLogAppender.captured();
            if (!log.isEmpty()) {
                scenario.attach(log, "text/plain", "Scenario log");
            }
        }
    }
}
