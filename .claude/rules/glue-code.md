---
paths:
  - "src/test/java/**/*.java"
---

# Step definitions, pages, support code
- Assert with TestNG `Assert.*`, always passing a message argument; use `context.softly()` for soft checks (`ReportingHooks` calls `assertAll()`).
- One page object per screen, each with a unique `pageMarker()`; parts shared across screens go in `pages/components/`. Locators are `private static final By`; interact through `UiObject` helpers (`click`, `type`, `text`, `visible`), which wait explicitly. Scope component locators to a root element instead of building selectors from data.
- Pages hold no assertions and return `void`; steps assert and call `waitUntilLoaded()` on the page they navigate to.
- Steps reach the browser through page objects; generic browser steps use `Browser` in `CommonSteps`.
- Share values between steps with `context.put/get`; inject `TestDataFactory` for test data (per-scenario Faker, `-Dfaker.seed` for reproducibility).
- Log business actions at INFO and element-level detail at DEBUG with parameterized messages (`log.info("Add '{}' to cart", name)`); pass exceptions as the last argument. Steps don't log their own start/end (`ExecutionLogger` does).
- Javadoc every public/protected type, constructor, method and constant (`@param`, `@return`, `@throws`); step definitions quote their Gherkin step in `{@code ...}`. Verify with `./gradlew testJavadoc`.
- Attach report artifacts with `scenario.attach(...)` (reaches both Allure and Cucumber HTML) instead of writing files.
