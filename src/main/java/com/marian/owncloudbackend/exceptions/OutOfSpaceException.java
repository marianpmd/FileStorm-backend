package com.marian.owncloudbackend.exceptions;

public class OutOfSpaceException extends RuntimeException {
    public OutOfSpaceException(String message) {
        super(message);
    }
}
