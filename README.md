# Selenium + Cucumber + TestNG seed

A starter project for end-to-end browser tests:

- **Gherkin feature files** run by **Cucumber 7**, executed through **TestNG** (assertions use TestNG `Assert` / `SoftAssert`)
- **Selenium 4** with local Chrome / Firefox / Edge (drivers downloaded automatically) or a remote **Selenium Grid**
- **Screenshots before and after every step**, plus page source, URL and browser console log on failure
- **Allure** report and Cucumber HTML/JSON reports
- Parallel scenarios, per-environment config, rerun of failed scenarios, REST Assured API helper, Datafaker test data
- Docker Compose grid (with optional video recording) and a GitHub Actions workflow

The sample tests run against the public demo shop [saucedemo.com](https://www.saucedemo.com) and the fake REST API [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com). Replace them with your own.

## Prerequisites

- JDK 21 (Gradle is provided by the wrapper — no install needed)
- At least one of Chrome, Firefox or Edge
- Optional: Docker, for the local grid

## Quick start

```bash
./gradlew test                      # all scenarios, Chrome, visible browser (gradlew.bat on Windows cmd)
./gradlew test -Dheadless=true      # same, headless
./gradlew allureServe               # build the Allure report and open it in the browser
```

Reports after a run:

| Report | Location |
|---|---|
| Allure (run `./gradlew allureReport` or `allureServe` first) | `build/reports/allure-report/allureReport/index.html` |
| Cucumber HTML (self-contained, screenshots embedded) | `build/reports/cucumber/cucumber.html` |
| Cucumber JSON (for CI plugins) | `build/reports/cucumber/cucumber.json` |
| Gradle/TestNG summary | `build/reports/tests/test/index.html` |
| Log file (every line tagged with thread and scenario) | `build/logs/e2e.log` (`e2e-rerun.log` for `rerunFailed`) |

## Running tests

Every setting can be passed as `-Dkey=value` (see [config](#configuration)). The most useful ones:

| What | Command |
|---|---|
| Only some scenarios, by tag | `./gradlew test -Dcucumber.filter.tags="@smoke"` |
| Tag expressions | `-Dcucumber.filter.tags="@cart and not @slow"` |
| One feature file | `-Dcucumber.features=src/test/resources/features/login.feature` |
| One scenario (by line) | `-Dcucumber.features=src/test/resources/features/login.feature:11` |
| Another browser | `-Dbrowser=firefox` / `-Dbrowser=edge` |
| Another environment | `-Denv=staging` (loads `config/staging.properties`) |
| More/fewer parallel scenarios | `-Dthreads=8` (`-Dthreads=1` for debugging) |
| Fewer screenshots (faster) | `-Dscreenshot.mode=failures` or `none` |
| Show console output in Gradle | `-Pverbose` |
| Keep Allure results of previous runs | `-PkeepAllureResults` |
| More console output | `-Dlog.console.level=DEBUG` (file always gets `log.level`, default DEBUG) |
| Reproducible fake data | `-Dfaker.seed=42` |

Note: `--tests` doesn't work for selecting scenarios; use tags or `cucumber.features`.

### Rerunning failed scenarios

The main run writes failed scenarios to `build/rerun/failed.txt`. Rerun only those:

```bash
./gradlew test -PignoreFailures    # don't stop the build when scenarios fail
./gradlew rerunFailed              # skipped automatically if nothing failed
./gradlew allureReport             # Allure shows the rerun as a retry of the original result
```

Try it with the intentionally failing example: `./gradlew test -Dcucumber.filter.tags=@known-failure -PignoreFailures`.

### Running in an IDE

Run `src/test/resources/testng.xml` as a TestNG suite, or the `CucumberTestRunner` class. To run a single scenario from the IDE, add `-Dcucumber.features=src/test/resources/features/login.feature:11` to the run configuration's VM options. Recommended VS Code extensions are listed in `.vscode/extensions.json` (Java, Gradle, Cucumber).

## Configuration

Config files live in `src/test/resources/config/`. Values are resolved in this order, **last wins**:

1. `default.properties` — every key with its default value and an explanation
2. `<env>.properties` — selected with `-Denv=...` or the `ENV` environment variable (default `local`)
3. Environment variables — key upper-cased with `.`/`-` replaced by `_`, e.g. `BASE_URL`, `TIMEOUT_EXPLICIT`
4. `-D` system properties

Add an environment by creating `config/<name>.properties` with only the keys that differ. Keep secrets out of the files: pass them as environment variables (e.g. from CI secrets) and read them with `Config.get().get("my.secret")`.

## Writing a new test

1. **Describe the behaviour** in a feature file under `src/test/resources/features/`:

   ```gherkin
   @ui @profile
   Feature: User profile
     Scenario: Change display name
       Given I am logged in as the "standard" user
       When I change my display name to "Ada"
       Then my profile should show "Ada"
   ```

2. **Create a page object** in `pages/` extending `BasePage`, one class per screen. Keep locators private, expose user-level actions, and name an element only this page shows as its `pageMarker()`:

   ```java
   public class ProfilePage extends BasePage {
       private static final By NAME = By.id("display-name");
       private static final By SAVE = By.cssSelector("[data-test='save']");

       public ProfilePage(Browser browser) { super(browser); }

       @Override protected By pageMarker() { return NAME; }

       public void changeName(String name) { type(NAME, name); click(SAVE); }
       public String name() { return text(NAME); }
   }
   ```

   The helpers (`click`, `type`, `typeSecret`, `text`, `visible`, `selectByValue`, ...) come from `UiObject` and wait explicitly, so there are no `Thread.sleep`s or implicit waits. Parts that appear on several pages (a header, a product tile) are components in `pages/components/` (see `HeaderComponent`, `ProductCard`).

3. **Write step definitions** in `steps/`. Ask for what you need in the constructor (page objects, components, `Browser`, `Config`, `TestContext`, `ApiClient`, `TestDataFactory`) and PicoContainer injects fresh instances for every scenario. After a navigation, call `waitUntilLoaded()` on the page you expect, so a wrong navigation fails right there:

   ```java
   public class ProfileSteps {
       private final ProfilePage profile;
       public ProfileSteps(ProfilePage profile) { this.profile = profile; }

       @When("I change my display name to {string}")
       public void changeName(String name) { profile.waitUntilLoaded(); profile.changeName(name); }

       @Then("my profile should show {string}")
       public void profileShows(String expected) { Assert.assertEquals(profile.name(), expected, "Display name"); }
   }
   ```

   Reuse existing steps where you can (`CommonSteps`, `LoginSteps`, `ApiSteps`). Running a feature with a missing step prints a ready-to-paste snippet.

4. **Run it**: `./gradlew test -Dcucumber.filter.tags=@profile`.

### Useful building blocks

| Need | Use |
|---|---|
| Share data between steps of a scenario | `context.put("orderId", id)` / `context.get("orderId", String.class)` |
| Several checks reported together | `context.softly().assertEquals(...)` — evaluated after the scenario |
| Random test data | inject `TestDataFactory`: `testData.faker()` / `testData.randomCustomer()` |
| Config values | inject `Config`: `config.get("my.key")` |
| Navigate, title, URL, custom waits | inject `Browser`: `browser.open("/path")`, `browser.waits().until(...)` |
| Support a new browser | implement `driver/BrowserSetup`, add a constant to `BrowserType` |
| Table → object | `@DataTableType` in `data/DataTableTypes.java` (see `Customer`) |
| Custom step parameter | `@ParameterType` in `data/DataTableTypes.java` (see `{sortOrder}`) |
| Call an API for setup/verification | inject `ApiClient`; requests/responses are attached to Allure |
| Scenario that doesn't need a browser | tag it `@api` |
| Attach something to the report | `context.scenario().attach(bytes, "text/plain", "name")` |

### Tag conventions

| Tag | Meaning |
|---|---|
| `@ui` / `@api` | Browser test / API-only test (`@api` doesn't start a browser) |
| `@smoke` | Small, fast subset for quick feedback |
| `@wip` | Work in progress — excluded from normal runs |
| `@known-failure` | Known product bug — excluded from normal runs |
| `@issue=ABC-123`, `@tmsLink=TC-42` | Link to tracker / test case in Allure (set URL patterns in `allure.properties`) |
| `@<feature-area>` | e.g. `@login`, `@cart`, `@checkout`, for selecting areas |

## Screenshots and failure artifacts

With `screenshot.mode=all` (default), every step gets a screenshot before and after it runs, labelled with the step text. They appear in Allure ("Screenshot before/after: ...") and in the Cucumber HTML report. When a scenario fails, the browser URL/title (and grid session id), page source and browser console log (Chrome/Edge only) are attached as well. Use `screenshot.mode=failures` for one screenshot per failed scenario, or `none` to turn them off.

## Logging

Everything logs through **SLF4J → Logback** (`src/test/resources/logback-test.xml`). Selenium's `java.util.logging` and Apache HttpClient's commons-logging are bridged into it, so there is one log.

- **What's logged:** effective config with the source of each value (secrets masked), browser start/stop with version, session id and timing, every scenario and step with status and duration (`ExecutionLogger` plugin), page actions (INFO) and element interactions (DEBUG), wait timeouts with the unmet condition, API calls (INFO one line, DEBUG headers/bodies), test data, browser console errors on failure.
- **Where:** console (INFO by default), `build/logs/e2e.log` (DEBUG), and each scenario's own lines as a **"Scenario log" attachment** in Allure and Cucumber HTML (`log.attach=all|failures|none`).
- **Levels:** `-Dlog.level=INFO|DEBUG|TRACE` for framework loggers, `-Dlog.console.level=...` for the console, `-Dlog.file=...` for the file location. Library levels are set in `logback-test.xml`.
- **Secrets:** config keys and HTTP headers named like password/secret/token/key/cookie are masked, as are quoted values after such words in step text (`password "******"`). Type passwords with `typeSecret(...)`.
- **In your code:** page objects have a ready-made `log` field; elsewhere use `private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);`. Use parameterized messages (`LOG.info("Added {}", item)`), and pass exceptions as the last argument. Don't log step start/end yourself — `ExecutionLogger` does it.

## Selenium Grid

```bash
./gradlew gridUp                # hub + Chrome, Firefox and Edge nodes in Docker; UI at http://localhost:4444/ui
./gradlew test -Denv=grid       # or: -Dexecution=remote -Dgrid.url=http://my-grid:4444
./gradlew gridDown
```

Video recording: `./gradlew gridUp -Pvideo` adds a recorder per node. Videos are written to `build/videos/<session id>.mp4`; the session id is attached to failed scenarios. While recording, each node runs one session at a time.

Any other grid (Selenium Grid, Selenoid, cloud providers) works by pointing `grid.url` at it.

## CI (GitHub Actions)

`.github/workflows/e2e.yml` runs on pushes to `main`, pull requests, weekday mornings and on demand (with `env` and `tags` inputs). It runs Chrome and Firefox in parallel, headless, reruns failed scenarios once, builds the Allure report, and uploads all reports and logs as artifacts. The job fails only if scenarios still fail after the rerun.

## Project layout

```
src/test/java/com/example/e2e/
  runners/   TestNG entry points: CucumberTestRunner (main), RerunFailedRunner, ParallelismListener
  hooks/     BrowserHooks (start/quit), ScreenshotHooks, ReportingHooks (soft asserts, failure artifacts, logs); HookOrder
  context/   TestContext: per-scenario state shared by steps (scenario, soft asserts, values)
  driver/    Browser (per-scenario session), WebDriverFactory, BrowserType + one BrowserSetup per browser
  config/    Config (layered properties), ConfigKeys
  pages/     UiObject (helpers), BasePage, page objects; components/ for shared page parts
  steps/     step definitions
  api/       ApiClient (REST Assured)
  data/      test data: Datafaker factory, DataTable/parameter types
  support/   Waits, ScreenshotService, BrowserConsole, Allure environment, StepTracker plugin
  logging/   SLF4J bootstrap, ExecutionLogger plugin, per-scenario log capture, masking
src/test/resources/
  features/  .feature files
  config/    <env>.properties
  testng.xml, testng-rerun.xml, allure.properties, categories.json, logback-test.xml
docker/      Selenium Grid compose files
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `SessionNotCreatedException` because a driver/browser version doesn't match | Selenium Manager caches drivers in `~/.cache/selenium`; delete it and rerun. |
| First page load takes ~30s and pages time out | Check for local ad blockers or traffic filters (e.g. AdGuard) injecting scripts into the test browser; exclude the browser from filtering. |
| Element not found / flaky clicks | Use `BasePage` helpers (they wait); for custom conditions use `browser.waits().until(ExpectedConditions...)`. Increase `timeout.explicit` for slow environments. |
| Tests interfere with each other in parallel | Don't store state in static fields; use `TestContext` or another injected object. Test data must be unique per scenario (use Datafaker). |
| No console logs in report for Firefox | Firefox doesn't expose them through WebDriver; this is expected. |
