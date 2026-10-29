/**
 * End-to-end browser and API tests: Gherkin features run by Cucumber through TestNG, driving the browser with Selenium.
 * <p>
 * How the packages fit together:
 * <ul>
 *   <li>{@link com.example.e2e.runners} - TestNG entry points that hand the scenarios to Cucumber</li>
 *   <li>{@link com.example.e2e.steps} - step definitions that map Gherkin steps to page objects and API calls</li>
 *   <li>{@link com.example.e2e.pages} - page objects and components; the only code that knows locators</li>
 *   <li>{@link com.example.e2e.driver} - the per-scenario {@link com.example.e2e.driver.Browser} and browser setups</li>
 *   <li>{@link com.example.e2e.hooks} - scenario lifecycle: browser start/stop, screenshots, reporting</li>
 *   <li>{@link com.example.e2e.config}, {@link com.example.e2e.context}, {@link com.example.e2e.data},
 *       {@link com.example.e2e.api}, {@link com.example.e2e.support}, {@link com.example.e2e.logging} - shared services</li>
 * </ul>
 * Glue classes, page objects and services are created per scenario by PicoContainer and receive their
 * dependencies through constructors.
 */
package com.example.e2e;
