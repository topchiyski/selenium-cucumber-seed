package com.example.e2e.data;

import com.example.e2e.context.TestContext;
import io.cucumber.java.Scenario;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Random;

/**
 * Random but realistic test data (Datafaker). PicoContainer creates one per scenario, each with its own
 * {@link Faker}, so parallel scenarios never share a generator.
 * <p>
 * With {@code -Dfaker.seed=123} the data is reproducible: each scenario's Faker is seeded from that seed
 * combined with the scenario's feature file and line. A scenario gets the same data on every run, but no two
 * scenarios get the same data, so parallel scenarios don't create colliding records (e.g. the same user).
 */
public class TestDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataFactory.class);

    private final TestContext context;
    private Faker faker;

    /**
     * Creates the factory for one scenario; the Faker is created on first use.
     *
     * @param context identifies the running scenario, for per-scenario seeds
     */
    public TestDataFactory(TestContext context) {
        this.context = context;
    }

    /**
     * The scenario's Faker, for data this class has no helper for.
     *
     * @return the Faker instance
     */
    public Faker faker() {
        if (faker == null) {
            faker = createFaker();
        }
        return faker;
    }

    /**
     * Generates a customer with a realistic name and postal code.
     *
     * @return a new random customer
     */
    public Customer randomCustomer() {
        Faker f = faker();
        Customer customer = new Customer(f.name().firstName(), f.name().lastName(), f.address().zipCode());
        LOG.debug("Generated {}", customer);
        return customer;
    }

    private Faker createFaker() {
        String seed = System.getProperty("faker.seed");
        if (seed == null) {
            LOG.debug("Creating Faker (random seed)");
            return new Faker(Locale.ENGLISH);
        }
        long scenarioSeed = scenarioSeed(Long.parseLong(seed));
        LOG.debug("Creating Faker (seed {} for this scenario, from faker.seed={})", scenarioSeed, seed);
        return new Faker(Locale.ENGLISH, new Random(scenarioSeed));
    }

    /** Mixes the scenario's location into the seed; String.hashCode is stable across JVMs. */
    private long scenarioSeed(long baseSeed) {
        Scenario scenario = context.scenario();
        if (scenario == null) {
            LOG.warn("Test data requested before the scenario started; using faker.seed unchanged");
            return baseSeed;
        }
        String location = scenario.getUri() + ":" + scenario.getLine();
        return baseSeed * 31 + location.hashCode();
    }
}
