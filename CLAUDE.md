# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

E2E browser-test seed: Gherkin → Cucumber 7 → TestNG, Selenium 4, Gradle (Kotlin DSL), Java 21. All code lives in `src/test`; there is no `src/main`. Samples (saucedemo.com UI, jsonplaceholder API) are placeholders to replace.

## Invariants
- IMPORTANT: Get dependencies by constructor injection (PicoContainer: `Browser`, `Config`, pages, `TestContext`, ...) and keep per-scenario state in those objects. Scenarios run in parallel threads; static mutable fields leak between them. Use `Config.get()` only where PicoContainer can't reach (TestNG listener, Cucumber plugins, static `@BeforeAll`).
- IMPORTANT: Keep the `webdriver.httpclient.version=HTTP_1_1` default in `driver/WebDriverFactory`.
  <!-- 2026-10-29: Triggered by the first ChromeDriver session of every JVM hanging 3 min (JDK HttpClient HTTP/2 upgrade; chromedriver log showed no InitSession). Outcome: 6/6 sessions started in <1s after forcing 1.1. Prune when: removing it still starts 6 consecutive sessions in a fresh JVM. -->
- Log via SLF4J only: `log` field in page objects, `LoggerFactory.getLogger(X.class)` elsewhere; never `System.out` or `e.printStackTrace()`. Type secrets with `BasePage.typeSecret`; mask other secrets with `logging/Masking`.
- New config key with a new prefix → add the prefix to `forwardedPropertyPrefixes` in `build.gradle.kts`, or Gradle silently drops `-Dkey=...`.

## Commands
- Fast verify after every edit: `./gradlew compileTestJava -q`
- One scenario (preferred while iterating): `./gradlew test -Dheadless=true -Dcucumber.features=src/test/resources/features/<file>.feature:<line>`
- By tag: `./gradlew test -Dheadless=true -Dcucumber.filter.tags="@smoke"`
- Full suite (~20s, needs internet): `./gradlew test -Dheadless=true`
- Javadoc + doclint (must stay warning-free): `./gradlew testJavadoc` → `build/docs/testJavadoc/`
- Rerun failures: `./gradlew test -PignoreFailures && ./gradlew rerunFailed`
- Reports: `./gradlew allureReport` → `build/reports/allure-report/allureReport/`; Cucumber HTML → `build/reports/cucumber/cucumber.html`; log → `build/logs/e2e.log`
- Select scenarios with tags or `cucumber.features`; `--tests` matches nothing. `-Pverbose` shows test stdout. No linter or formatter is configured.

## Where things go
| Change | Location | Pattern |
|---|---|---|
| Scenario | `src/test/resources/features/**/*.feature` | see `.claude/rules/features.md` |
| Page object (one per screen) | `pages/`, extends `BasePage(Browser)` | private `By` locators, `UiObject` helpers, `pageMarker()` |
| Shared page part | `pages/components/` | extends `UiObject`, or wraps a root `WebElement` (`ProductCard`) |
| Step definitions | `steps/` | inject pages / components / `Browser` / `Config` / `TestContext`; `waitUntilLoaded()` after navigating |
| Config key | `config/default.properties` + `ConfigKeys` | read via `Config.get()` |
| Browser options / new browser | `driver/<X>Setup` implements `BrowserSetup` | add constant to `BrowserType`; shared logic in `WebDriverFactory` |
| New runner | `runners/` + its own testng xml | include `StepTracker`, `ExecutionLogger` and `AllureCucumber7Jvm` in `plugin` |
| Visual checkpoint | `@visual` scenario + `VisualSteps`, or inject `visual/VisualCheck` | key only via `APPLITOOLS_API_KEY` env; fixed test data |
| Log levels / appenders | `src/test/resources/logback-test.xml` | `-Dlog.level`, `-Dlog.console.level`, `-Dlog.file` |

## Architecture
- `test` → `testng.xml` → `CucumberTestRunner` (parallel `@DataProvider`, one TestNG test per scenario; thread count from config `threads` via `ParallelismListener`). `rerunFailed` → `testng-rerun.xml` → `RerunFailedRunner` (`@build/rerun/failed.txt`).
- Config layering, last wins: `config/default.properties` → `config/{env}.properties` (`-Denv`, default `local`) → env vars (`BASE_URL`) → `-D`.
- `driver/Browser` is the per-scenario session (PicoContainer-scoped, `Disposable`). `BrowserHooks` starts it for non-`@api` scenarios and quits it. Implicit wait is 0: wait with `UiObject` helpers or `browser.waits()` instead of `Thread.sleep`.
- Hooks are split by concern (`BrowserHooks`, `ScreenshotHooks`, `ReportingHooks`, `VisualHooks`); all orders live in `hooks/HookOrder`. `@After` runs highest order first.
- Step screenshots: `support/StepTracker` (Cucumber `ConcurrentEventListener`) records step texts per thread on `TestCaseStarted`; `ScreenshotHooks` indexes them and renames the Allure hook step via `Allure.getLifecycle().updateStep`. Mode: `screenshot.mode=all|failures|none`.
- Logging: SLF4J → Logback; JUL (Selenium) bridged by `logging/LoggingBootstrap`, commons-logging by `jcl-over-slf4j`. `logging/ExecutionLogger` (Cucumber plugin) logs scenarios/steps, sets MDC `scenario` and starts `ScenarioLogAppender` capture; `ReportingHooks` attaches it as "Scenario log" (`log.attach`).
- `test` wipes `build/allure-results` (unless `-PkeepAllureResults`); `rerunFailed` keeps it so reruns show as retries.

## Pinned choices
- Cucumber 7.x, not 8.
  <!-- 2026-10-29: allure-cucumber7-jvm is the newest Allure adapter; no cucumber8 adapter on Maven Central. Prune when: io.qameta.allure:allure-cucumber8-jvm (or similar) is published. -->
- `isScanForTestClasses = false` + explicit runner include in `build.gradle.kts`.
  <!-- 2026-10-29: Gradle 9.8 reported "no tests discovered" because the runner's @Test is inherited from cucumber-testng. Outcome: tests run with scanning off. Prune when: scanning on discovers CucumberTestRunner. -->
- `page-load.strategy=normal` default.
  <!-- 2026-10-29: eager did not help with 30s page loads; the cause was AdGuard on the dev machine injecting a script. Prune when: never — this is just the WebDriver default. -->

## Done criteria
1. `./gradlew compileTestJava -q` passes.
2. Affected scenarios pass, run by tag or `feature:line`, headless. Report pass/fail counts; on failure, read `build/logs/e2e.log` and the Allure attachments before changing code.
3. `./gradlew testJavadoc` reports no warnings: every public/protected type, constructor, method and constant has Javadoc with `@param`/`@return`/`@throws`.
4. Edits to `README.md` run-options or config tables stay in sync with `default.properties` and `build.gradle.kts`.
