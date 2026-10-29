package com.example.e2e.context;

import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Per-scenario state shared between hooks and step definitions: the running {@link Scenario}, soft
 * assertions, and values that later steps need. The browser and config are separate injectables
 * ({@code Browser}, {@code Config}) so classes only depend on what they use.
 * <p>
 * PicoContainer creates one instance per scenario and injects it into any step definition or hook class
 * that asks for it in its constructor — never store it in a static field.
 */
public class TestContext {

    private static final Logger LOG = LoggerFactory.getLogger(TestContext.class);

    private final Map<String, Object> data = new HashMap<>();
    private final SoftAssert softAssert = new SoftAssert();
    private Scenario scenario;

    /** Created by PicoContainer, once per scenario. */
    public TestContext() {
    }

    /**
     * Collects non-fatal checks; they are evaluated together after the scenario by {@code ReportingHooks}.
     *
     * @return the scenario's soft assertions
     */
    public SoftAssert softly() {
        return softAssert;
    }

    /**
     * The running scenario, e.g. to attach extra artifacts to the report.
     *
     * @return the scenario set by {@code ReportingHooks} before the first step
     */
    public Scenario scenario() {
        return scenario;
    }

    /**
     * Stores the running scenario; called by {@code ReportingHooks}.
     *
     * @param scenario the scenario that is about to run
     */
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /**
     * Stores a value so later steps in the same scenario can use it.
     *
     * @param key name to store the value under
     * @param value the value; replaces an earlier value with the same key
     */
    public void put(String key, Object value) {
        LOG.debug("Context put {} = {}", key, value);
        data.put(key, value);
    }

    /**
     * Returns a value stored by an earlier step of this scenario.
     *
     * @param key the key it was stored under
     * @param type the expected type
     * @param <T> the expected type
     * @return the value
     * @throws java.util.NoSuchElementException if nothing is stored under {@code key}
     * @throws ClassCastException if the value is not a {@code type}
     */
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            throw new NoSuchElementException("No value stored in scenario context under '" + key + "'");
        }
        return type.cast(value);
    }

    /**
     * Whether an earlier step stored a value under this key.
     *
     * @param key the key to check
     * @return {@code true} if a value is stored
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }
}
