package com.example.e2e.data;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Random;

/**
 * Random but realistic test data (Datafaker). PicoContainer creates one per scenario, each with its own
 * {@link Faker}. Set {@code -Dfaker.seed=123} to make every scenario's data reproducible.
 */
public class TestDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataFactory.class);

    private final Faker faker;

    /** Creates a factory with a new {@link Faker}, seeded from {@code -Dfaker.seed} if set. */
    public TestDataFactory() {
        String seed = System.getProperty("faker.seed");
        LOG.debug("Creating Faker (seed: {})", seed == null ? "random" : seed);
        this.faker = seed == null ? new Faker(Locale.ENGLISH) : new Faker(Locale.ENGLISH, new Random(Long.parseLong(seed)));
    }

    /**
     * The scenario's Faker, for data this class has no helper for.
     *
     * @return the Faker instance
     */
    public Faker faker() {
        return faker;
    }

    /**
     * Generates a customer with a realistic name and postal code.
     *
     * @return a new random customer
     */
    public Customer randomCustomer() {
        Customer customer = new Customer(faker.name().firstName(), faker.name().lastName(), faker.address().zipCode());
        LOG.debug("Generated {}", customer);
        return customer;
    }
}
