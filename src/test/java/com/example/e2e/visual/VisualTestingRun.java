package com.example.e2e.visual;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.TestResultContainer;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.ClassicRunner;
import com.example.e2e.config.Config;
import com.example.e2e.config.ConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State shared by all visual checks of one test run: a single {@link ClassicRunner} and a single
 * {@link BatchInfo}, so every scenario (including parallel ones) reports into one batch in the Applitools
 * dashboard. Created on first use; {@link #summarize()} logs the outcome at the end of the run.
 */
public final class VisualTestingRun {

    private static final Logger LOG = LoggerFactory.getLogger(VisualTestingRun.class);

    private static ClassicRunner runner;
    private static BatchInfo batch;

    private VisualTestingRun() {
    }

    /**
     * The run's Eyes runner, created on first call.
     *
     * @return the shared runner
     */
    public static synchronized ClassicRunner runner() {
        if (runner == null) {
            runner = new ClassicRunner();
        }
        return runner;
    }

    /**
     * The run's batch, created on first call from {@code applitools.batch-name} and, if set,
     * {@code applitools.batch-id}.
     *
     * @param config the run's configuration
     * @return the shared batch
     */
    public static synchronized BatchInfo batch(Config config) {
        if (batch == null) {
            batch = new BatchInfo(config.get(ConfigKeys.APPLITOOLS_BATCH_NAME));
            config.find(ConfigKeys.APPLITOOLS_BATCH_ID).ifPresent(batch::setId);
            LOG.info("Applitools batch '{}' (id {})", config.get(ConfigKeys.APPLITOOLS_BATCH_NAME), batch.getId());
        }
        return batch;
    }

    /** Logs passed, failed and new visual tests of the run; does nothing if no visual check ran. */
    public static synchronized void summarize() {
        if (runner == null) {
            return;
        }
        TestResultsSummary summary = runner.getAllTestResults(false);
        int passed = 0;
        int unresolved = 0;
        int created = 0;
        for (TestResultContainer container : summary.getAllResults()) {
            TestResults results = container.getTestResults();
            if (results == null) {
                LOG.warn("Visual test without results", container.getException());
            } else if (Boolean.TRUE.equals(results.isNew())) {
                created++;
            } else if (results.isPassed()) {
                passed++;
            } else {
                unresolved++;
            }
        }
        LOG.info("Applitools results: {} passed, {} with differences, {} new baselines", passed, unresolved, created);
    }
}
