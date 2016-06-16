package com.flipkart.polyguice.trooper;

import com.flipkart.polyguice.core.support.Polyguice;
import com.flipkart.polyguice.trooper.exceptions.LifeCycleException;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A container for multiple PolyGuice containers.
 * Each container is referred to as a "trooper".
 *
 */
public class PolyContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolyContainer.class);
    private Set<Polyguice> polyTroopers;
    private Set<LifeCycleListener> lifeCycleListeners;
    private boolean started;

    public PolyContainer() {
        polyTroopers = new HashSet<>();
        lifeCycleListeners = new HashSet<>();
        started = false;
    }

    /**
     * the container registers a shutdown hook and stops itself with Runtime shutdown
     */
    public PolyContainer bindWithAppLifeCycle() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        return this;
    }


    public PolyContainer register(Polyguice polyguice) {
        this.polyTroopers.add(polyguice);
        return this;
    }

    /**
     * Registers a lifecycle listener. We can register
     * @param lifeCycleListener The lifecycle listener that will be invoked before managing lifecycle calls of registered polyguice containers
     */
    public PolyContainer register(LifeCycleListener lifeCycleListener) {
        lifeCycleListeners.add(lifeCycleListener);
        return this;
    }

    public void start() {
        if (started) {
            LOGGER.debug("Not initialising an already started container. We;re not crazy that way");
            return;
        }
        lifeCycleListeners.forEach(LifeCycleListener::preStart);
        for (Polyguice polyTrooper : polyTroopers) {
            polyTrooper.prepare();
        }
        this.started = true;
    }

    public void stop() {
        List<Pair<Polyguice,String>> failedStops = new ArrayList<>();
        List<Pair<LifeCycleListener,String>> failedCallbacks = new ArrayList<>();
        if (!started) {
            LOGGER.debug("Not attempting to stop that which is not started. We;re not crazy that way");
            return;
        }
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            try {
                lifeCycleListener.preStop();
            } catch (RuntimeException e) {
                LOGGER.warn("Call to preStop failed for {}, with exception {} ",lifeCycleListener,e.getMessage());
                failedCallbacks.add(new Pair<>(lifeCycleListener,e.getMessage()));
            }
        }

        for (Polyguice polyTrooper : polyTroopers) {
            try {
                polyTrooper.stop();
            } catch (RuntimeException e) {
                LOGGER.warn("Could not stop trooper {}, which failed with exception {} ",polyTrooper,e.getMessage());
                failedStops.add(new Pair<>(polyTrooper,e.getMessage()));
            }
        }
        started = false;
        if (!failedStops.isEmpty() || !failedCallbacks.isEmpty() ) {
            raiseException(failedStops,failedCallbacks);
        }
    }

    //// Helper Methods


    private void raiseException(List<Pair<Polyguice, String>> failedStops, List<Pair<LifeCycleListener, String>> failedCallbacks) {
        String msg = "Stop operation failed for ";
        for (Pair<Polyguice, String> failedStop : failedStops) {
            msg = msg + failedStop + ",";
        }
        for (Pair<LifeCycleListener,String> failedCallback : failedCallbacks) {
            msg = msg + failedCallback + ",";
        }
        throw new LifeCycleException(msg);
    }
}
