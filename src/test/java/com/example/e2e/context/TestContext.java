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
 * PicoContainer creates one instance per scenario and injects it into any glue class or page object
 * that asks for it in its constructor — never store it in a static field.
 */
public class TestContext {

    private static final Logger LOG = LoggerFactory.getLogger(TestContext.class);

    private final Map<String, Object> data = new HashMap<>();
    private final SoftAssert softAssert = new SoftAssert();
    private Scenario scenario;

    /** Collects non-fatal checks; they are evaluated together after the scenario by {@code Hooks}. */
    public SoftAssert softly() {
        return softAssert;
    }

    public Scenario scenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /** Stores a value so later steps in the same scenario can use it. */
    public void put(String key, Object value) {
        LOG.debug("Context put {} = {}", key, value);
        data.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            throw new NoSuchElementException("No value stored in scenario context under '" + key + "'");
        }
        return type.cast(value);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }
}
