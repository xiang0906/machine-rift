package com.machinerift.machine_rift.exception;

/**
 * Exception used when an operation conflicts with existing related data.
 */
public class ResourceConflictException extends RuntimeException {

    /**
     * Creates an exception with a message safe to return to API clients.
     *
     * @param message detail message
     */
    public ResourceConflictException(String message) {
        super(message);
    }
}
