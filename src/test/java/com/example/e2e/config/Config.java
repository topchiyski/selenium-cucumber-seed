package com.example.e2e.config;

import com.example.e2e.logging.Masking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Layered, read-only configuration. Values are resolved in this order (last wins):
 * <ol>
 *   <li>{@code config/default.properties}</li>
 *   <li>{@code config/{env}.properties}, where env comes from -Denv / ENV (default {@code local})</li>
 *   <li>environment variables, key upper-cased with '.' and '-' replaced by '_' (e.g. {@code BASE_URL})</li>
 *   <li>JVM system properties (-Dkey=value)</li>
 * </ol>
 * The files are read once per JVM; every {@code Config} instance is a cheap read-only view over them.
 * <p>
 * PicoContainer injects a {@code Config} into glue classes that ask for one in their constructor; prefer
 * that. {@link #get()} exists for code PicoContainer doesn't create (TestNG listeners, Cucumber plugins,
 * static {@code @BeforeAll} hooks).
 */
public final class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static final Config SHARED = new Config();

    private final Values values;

    /** Used by PicoContainer. */
    public Config() {
        this.values = Values.INSTANCE;
    }

    /**
     * The shared instance, for code that isn't created by PicoContainer.
     *
     * @return the shared instance
     */
    public static Config get() {
        return SHARED;
    }

    /**
     * Looks up a key through all configuration layers.
     *
     * @param key config key, e.g. {@code base.url}
     * @return the trimmed value, or empty if the key is missing or blank in every layer
     */
    public Optional<String> find(String key) {
        return values.resolve(key).map(Resolved::value);
    }

    private record Resolved(String value, String source) {
    }

    /** The loaded files. Initialized (and logged) once, on first use. */
    private static final class Values {

        private static final Values INSTANCE = new Values();

        private final String env;
        private final Map<String, String> fileValues = new HashMap<>();
        /** Which file each file value came from, for the startup log. */
        private final Map<String, String> fileSources = new HashMap<>();

        private Values() {
            this.env = Optional.ofNullable(System.getProperty(ConfigKeys.ENV))
                    .or(() -> Optional.ofNullable(System.getenv("ENV")))
                    .orElse("local");
            load("config/default.properties");
            load("config/" + env + ".properties");
            logEffectiveConfig();
        }

        private void load(String resource) {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("Config resource " + resource + " not found on the classpath (env=" + env + ")");
                }
                Properties properties = new Properties();
                properties.load(in);
                properties.stringPropertyNames().forEach(key -> {
                    fileValues.put(key, properties.getProperty(key));
                    fileSources.put(key, resource);
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + resource, e);
            }
        }

        private Optional<Resolved> resolve(String key) {
            String value = System.getProperty(key);
            String source = "-D system property";
            if (value == null) {
                String envVar = key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
                value = System.getenv(envVar);
                source = "environment variable " + envVar;
            }
            if (value == null) {
                value = fileValues.get(key);
                source = fileSources.get(key);
            }
            String trimmed = value == null ? null : value.trim();
            return trimmed == null || trimmed.isEmpty() ? Optional.empty() : Optional.of(new Resolved(trimmed, source));
        }

        /** INFO: env and the main settings; DEBUG: every key with its value (secrets masked) and where it came from. */
        private void logEffectiveConfig() {
            LOG.info("Configuration env={}: base.url={}, browser={}, headless={}, execution={}, threads={}",
                    env, value(ConfigKeys.BASE_URL), value(ConfigKeys.BROWSER), value(ConfigKeys.HEADLESS),
                    value(ConfigKeys.EXECUTION), value(ConfigKeys.THREADS));
            if (LOG.isDebugEnabled()) {
                for (String key : new TreeSet<>(fileValues.keySet())) {
                    resolve(key).ifPresentOrElse(
                            r -> LOG.debug("  {} = {}   [{}]", key, Masking.mask(key, r.value()), r.source()),
                            () -> LOG.debug("  {} = (empty)", key));
                }
            }
        }

        private String value(String key) {
            return resolve(key).map(Resolved::value).orElse("-");
        }
    }

    /**
     * Returns a required value.
     *
     * @param key config key
     * @return the trimmed value
     * @throws IllegalStateException if the key has no value in any layer
     */
    public String get(String key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing config key '" + key + "' (env=" + env() + ")"));
    }

    /**
     * Returns an optional value with a fallback.
     *
     * @param key config key
     * @param defaultValue returned when the key has no value
     * @return the value, or {@code defaultValue}
     */
    public String get(String key, String defaultValue) {
        return find(key).orElse(defaultValue);
    }

    /**
     * Returns a required integer value.
     *
     * @param key config key
     * @return the value as an {@code int}
     * @throws IllegalStateException if the key has no value
     * @throws NumberFormatException if the value is not an integer
     */
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Returns a required boolean value; only {@code true} (any case) counts as true.
     *
     * @param key config key
     * @return the value as a {@code boolean}
     * @throws IllegalStateException if the key has no value
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Returns a required duration configured as a whole number of seconds.
     *
     * @param key config key, e.g. {@code timeout.explicit}
     * @return the duration
     * @throws IllegalStateException if the key has no value
     * @throws NumberFormatException if the value is not an integer
     */
    public Duration getSeconds(String key) {
        return Duration.ofSeconds(getInt(key));
    }

    /**
     * The environment that selected {@code config/<env>.properties}.
     *
     * @return the name from {@code -Denv} or {@code ENV}, default {@code local}
     */
    public String env() {
        return values.env;
    }

    // Typed accessors for the keys used by the framework itself.

    /**
     * Base URL of the application under test.
     *
     * @return {@code base.url}
     */
    public String baseUrl() {
        return get(ConfigKeys.BASE_URL);
    }

    /**
     * Base URL for {@code ApiClient} requests.
     *
     * @return {@code api.base.url}
     */
    public String apiBaseUrl() {
        return get(ConfigKeys.API_BASE_URL);
    }

    /**
     * Whether browsers run without a visible window.
     *
     * @return {@code headless}
     */
    public boolean headless() {
        return getBoolean(ConfigKeys.HEADLESS);
    }

    /**
     * Whether browsers run on a Selenium Grid instead of locally.
     *
     * @return {@code true} when {@code execution=remote}
     */
    public boolean remote() {
        return "remote".equalsIgnoreCase(get(ConfigKeys.EXECUTION));
    }

    /**
     * Default timeout for explicit waits.
     *
     * @return {@code timeout.explicit}
     */
    public Duration explicitTimeout() {
        return getSeconds(ConfigKeys.TIMEOUT_EXPLICIT);
    }

    /**
     * When step and failure screenshots are taken.
     *
     * @return {@code screenshot.mode}
     */
    public AttachMode screenshotMode() {
        return attachMode(ConfigKeys.SCREENSHOT_MODE);
    }

    /**
     * When each scenario's log lines are attached to the report.
     *
     * @return {@code log.attach}
     */
    public AttachMode scenarioLogMode() {
        return attachMode(ConfigKeys.LOG_ATTACH);
    }

    private AttachMode attachMode(String key) {
        return AttachMode.valueOf(get(key).toUpperCase(Locale.ROOT));
    }

    /**
     * Number of scenarios run in parallel.
     *
     * @return {@code threads}
     */
    public int threads() {
        return getInt(ConfigKeys.THREADS);
    }

    /** When to attach an artifact (screenshots, scenario log) to the report. */
    public enum AttachMode {
        /** For every scenario (screenshots: before and after every step). */
        ALL,
        /** Only when a scenario fails. */
        FAILURES,
        /** Never. */
        NONE
    }
}
