package com.flipkart.polyguice.trooper;

/**
 * Gives capability to hook into the <Code>PolyContainer</Code>
 */
public interface LifeCycleListener {
    /* Called before the PolyContainer starts the registered PolyGuice instances */
    void preStart();
    /* Called before the PolyContainer stops the registered PolyGuice instances */
    void preStop();
}
