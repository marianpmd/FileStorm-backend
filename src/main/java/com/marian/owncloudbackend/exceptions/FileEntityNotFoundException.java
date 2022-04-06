package com.marian.owncloudbackend.exceptions;

public class FileEntityNotFoundException extends RuntimeException {
    public FileEntityNotFoundException(String message) {
        super(message);
    }
}
