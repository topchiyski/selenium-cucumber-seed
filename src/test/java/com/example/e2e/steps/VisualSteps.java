package com.example.e2e.steps;

import com.example.e2e.visual.VisualCheck;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Visual checkpoints with Applitools Eyes. Use them in scenarios tagged {@code @visual}; the comparison
 * result is evaluated when the scenario ends ({@code VisualHooks}).
 */
public class VisualSteps {

    private final VisualCheck visual;

    /**
     * Creates the steps for one scenario.
     *
     * @param visual the scenario's visual checks
     */
    public VisualSteps(VisualCheck visual) {
        this.visual = visual;
    }

    /**
     * {@code Then the "Checkout overview" page should look as expected}: a full-page checkpoint.
     *
     * @param name checkpoint name; keep it stable, it identifies the checkpoint in the baseline
     */
    @Then("the {string} page should look as expected")
    public void thePageShouldLookAsExpected(String name) {
        visual.check(name);
    }

    /**
     * {@code Then the "Products" page should look as expected ignoring:} followed by a one-column table of
     * CSS selectors for content that changes between runs (dates, order numbers, ads).
     *
     * @param name checkpoint name
     * @param cssSelectors elements to leave out of the comparison
     */
    @Then("the {string} page should look as expected ignoring:")
    public void thePageShouldLookAsExpectedIgnoring(String name, List<String> cssSelectors) {
        visual.check(name, cssSelectors.stream().map(By::cssSelector).toArray(By[]::new));
    }
}
