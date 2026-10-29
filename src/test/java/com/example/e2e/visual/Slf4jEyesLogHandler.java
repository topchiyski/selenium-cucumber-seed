package com.example.e2e.visual;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.logging.ClientEvent;
import com.applitools.eyes.logging.TraceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Routes the Applitools SDK's own log events into SLF4J, under the logger {@code com.applitools}. */
public class Slf4jEyesLogHandler extends LogHandler {

    private static final Logger LOG = LoggerFactory.getLogger("com.applitools");

    /** Creates a handler that forwards events from {@link TraceLevel#Info} upwards. */
    public Slf4jEyesLogHandler() {
        super(TraceLevel.Info);
    }

    /** Nothing to open; SLF4J is always ready. */
    @Override
    public void open() {
    }

    /**
     * Logs one SDK event at the matching SLF4J level.
     *
     * @param event the SDK's log event
     */
    @Override
    public void onMessageInner(ClientEvent event) {
        String message = String.valueOf(event.getEvent());
        TraceLevel level = event.getLevel();
        if (level == TraceLevel.Error) {
            LOG.error(message);
        } else if (level == TraceLevel.Warn) {
            LOG.warn(message);
        } else if (level == TraceLevel.Debug) {
            LOG.debug(message);
        } else {
            LOG.info(message);
        }
    }

    /** Nothing to close. */
    @Override
    public void close() {
    }

    /**
     * The handler is always open.
     *
     * @return {@code true}
     */
    @Override
    public boolean isOpen() {
        return true;
    }
}
