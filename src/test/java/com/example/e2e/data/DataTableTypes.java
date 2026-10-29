package com.example.e2e.data;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.util.Map;

/**
 * Custom Cucumber types. A {@link DataTableType} turns table rows into objects; a {@link ParameterType}
 * adds a placeholder usable in step expressions, e.g. {@code {sortOrder}}.
 */
public class DataTableTypes {

    private final TestDataFactory testData;

    /**
     * Creates the type registry for one scenario.
     *
     * @param testData generates values for {@code random} table cells
     */
    public DataTableTypes(TestDataFactory testData) {
        this.testData = testData;
    }

    /**
     * Converts a table with columns firstName | lastName | postalCode; the value "random" generates fake data.
     *
     * @param row one table row, keyed by column header
     * @return the customer
     */
    @DataTableType
    public Customer customer(Map<String, String> row) {
        Customer random = testData.randomCustomer();
        return new Customer(
                valueOrRandom(row.get("firstName"), random.firstName()),
                valueOrRandom(row.get("lastName"), random.lastName()),
                valueOrRandom(row.get("postalCode"), random.postalCode()));
    }

    /**
     * Maps readable sort names used in feature files to the values of the site's sort dropdown.
     *
     * @param label the readable sort name from the step
     * @return the dropdown value: {@code az}, {@code za}, {@code lohi} or {@code hilo}
     */
    @ParameterType("name \\(A to Z\\)|name \\(Z to A\\)|price \\(low to high\\)|price \\(high to low\\)")
    public String sortOrder(String label) {
        return switch (label) {
            case "name (A to Z)" -> "az";
            case "name (Z to A)" -> "za";
            case "price (low to high)" -> "lohi";
            case "price (high to low)" -> "hilo";
            default -> throw new IllegalArgumentException(label);
        };
    }

    private static String valueOrRandom(String value, String random) {
        if (value == null) {
            return "";
        }
        return "random".equalsIgnoreCase(value) ? random : value;
    }
}
