package com.example.e2e.logging;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Cucumber plugin that logs the run, every scenario and every step (with status and duration), so step
 * definitions don't need their own "starting step" log lines. It also owns the per-scenario logging
 * context: MDC {@code scenario} (shown in every log line) and {@link ScenarioLogAppender} capture.
 * <p>
 * A {@link ConcurrentEventListener} receives events on the thread running the scenario, which is what
 * makes the MDC and the thread-local capture line up with the scenario.
 */
public class ExecutionLogger implements ConcurrentEventListener {

    public static final String MDC_SCENARIO = "scenario";
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionLogger.class);

    private final Map<Status, AtomicInteger> scenarioCounts = new EnumMap<>(Status.class);

    public ExecutionLogger() {
        LoggingBootstrap.install();
        for (Status status : Status.values()) {
            scenarioCounts.put(status, new AtomicInteger());
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, e -> LOG.info("Test run started"));
        publisher.registerHandlerFor(TestCaseStarted.class, this::caseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::stepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::stepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::caseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::runFinished);
    }

    private void caseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        MDC.put(MDC_SCENARIO, testCase.getName());
        ScenarioLogAppender.startCapture();
        LOG.info("Scenario started: '{}' {} ({}:{})", testCase.getName(), testCase.getTags(),
                testCase.getUri(), testCase.getLocation().getLine());
    }

    private void stepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            LOG.debug("Step started: {}", stepText(step));
        } else if (event.getTestStep() instanceof HookTestStep hook) {
            LOG.trace("Hook started: {}", hook.getCodeLocation());
        }
    }

    private void stepFinished(TestStepFinished event) {
        Result result = event.getResult();
        long millis = result.getDuration().toMillis();
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            String text = stepText(step);
            switch (result.getStatus()) {
                case PASSED -> LOG.info("Step PASSED ({} ms): {}", millis, text);
                case FAILED -> LOG.error("Step FAILED ({} ms): {}", millis, text, result.getError());
                case SKIPPED -> LOG.debug("Step SKIPPED: {}", text);
                case UNDEFINED -> LOG.warn("Step UNDEFINED (no matching step definition): {}", text);
                default -> LOG.warn("Step {}: {}", result.getStatus(), text);
            }
        } else if (event.getTestStep() instanceof HookTestStep hook) {
            if (result.getStatus() == Status.FAILED) {
                LOG.error("Hook FAILED ({} ms): {}", millis, hook.getCodeLocation(), result.getError());
            } else {
                LOG.trace("Hook {} ({} ms): {}", result.getStatus(), millis, hook.getCodeLocation());
            }
        }
    }

    private void caseFinished(TestCaseFinished event) {
        try {
            Result result = event.getResult();
            scenarioCounts.get(result.getStatus()).incrementAndGet();
            String name = event.getTestCase().getName();
            long millis = result.getDuration().toMillis();
            if (result.getStatus() == Status.PASSED) {
                LOG.info("Scenario PASSED ({} ms): '{}'", millis, name);
            } else if (result.getStatus() == Status.FAILED) {
                LOG.error("Scenario FAILED ({} ms): '{}' - {}", millis, name, firstLine(result.getError()));
            } else {
                LOG.warn("Scenario {} ({} ms): '{}'", result.getStatus(), millis, name);
            }
        } finally {
            ScenarioLogAppender.stopCapture();
            MDC.remove(MDC_SCENARIO);
        }
    }

    private void runFinished(TestRunFinished event) {
        String counts = scenarioCounts.entrySet().stream()
                .filter(e -> e.getValue().get() > 0)
                .map(e -> e.getKey() + "=" + e.getValue().get())
                .collect(Collectors.joining(", "));
        LOG.info("Test run finished in {} s: {}", event.getResult().getDuration().toSeconds(), counts.isEmpty() ? "no scenarios" : counts);
    }

    private static String stepText(PickleStepTestStep step) {
        String text = (step.getStep().getKeyword() + step.getStep().getText()).trim();
        return Masking.maskInline(text) + " (line " + step.getStep().getLine() + ")";
    }

    private static String firstLine(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return error == null ? "" : error.getClass().getSimpleName();
        }
        return error.getMessage().lines().findFirst().orElse("");
    }
}
