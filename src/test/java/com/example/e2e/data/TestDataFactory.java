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

    public TestDataFactory() {
        String seed = System.getProperty("faker.seed");
        LOG.debug("Creating Faker (seed: {})", seed == null ? "random" : seed);
        this.faker = seed == null ? new Faker(Locale.ENGLISH) : new Faker(Locale.ENGLISH, new Random(Long.parseLong(seed)));
    }

    public Faker faker() {
        return faker;
    }

    public Customer randomCustomer() {
        Customer customer = new Customer(faker.name().firstName(), faker.name().lastName(), faker.address().zipCode());
        LOG.debug("Generated {}", customer);
        return customer;
    }
}
