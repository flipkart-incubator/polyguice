package com.flipkart.polyguice.trooper.exceptions;

/**
 * This exception is thrown when a Lifecycle operation fails.
 * In most cases, the container continues with any remainder lifecycle operations and raises this exception once all
 * operations are attempted
 */
public class LifeCycleException extends RuntimeException {
    public LifeCycleException(String message) {
        super(message);
    }
}
