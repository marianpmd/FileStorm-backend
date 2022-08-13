package com.marian.owncloudbackend.exceptions;

public class FileIsNotPublicException extends RuntimeException {
    public FileIsNotPublicException(String message) {
        super(message);
    }
}
