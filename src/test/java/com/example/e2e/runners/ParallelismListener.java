package com.example.e2e.runners;

import com.example.e2e.config.Config;
import com.example.e2e.logging.LoggingBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Sets TestNG's DataProvider thread count (= parallel scenarios) from the {@code threads} config key.
 * As the first framework code TestNG runs, it also installs the java.util.logging bridge.
 */
public class ParallelismListener implements IAlterSuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelismListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        LoggingBootstrap.install();
        int threads = Config.get().threads();
        suites.forEach(suite -> {
            suite.setDataProviderThreadCount(threads);
            LOG.info("Suite '{}': running scenarios on {} parallel thread(s)", suite.getName(), threads);
        });
    }
}
