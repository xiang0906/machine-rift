package com.machinerift.machine_rift.exception;

/**
 * Exception used when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
