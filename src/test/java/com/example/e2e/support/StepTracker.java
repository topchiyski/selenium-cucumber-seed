package com.example.e2e.support;

import com.example.e2e.logging.Masking;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCaseStarted;

import java.util.List;

/**
 * Cucumber plugin that records the Gherkin steps of the scenario running on each thread, so
 * {@code @BeforeStep}/{@code @AfterStep} hooks can label screenshots with the step text
 * (Cucumber's {@code Scenario} API doesn't expose the current step).
 * <p>
 * It's a {@link ConcurrentEventListener}, so events arrive on the thread executing the scenario.
 */
public class StepTracker implements ConcurrentEventListener {

    private static final ThreadLocal<List<String>> STEPS = ThreadLocal.withInitial(List::of);

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, event -> STEPS.set(event.getTestCase().getTestSteps().stream()
                .filter(PickleStepTestStep.class::isInstance)
                .map(PickleStepTestStep.class::cast)
                .map(s -> Masking.maskInline(s.getStep().getKeyword() + s.getStep().getText()))
                .toList()));
    }

    /** Text of the {@code index}-th step (0-based, Background steps included) of the current scenario. */
    public static String stepText(int index) {
        List<String> steps = STEPS.get();
        return index < steps.size() ? steps.get(index).trim() : "step " + (index + 1);
    }
}
