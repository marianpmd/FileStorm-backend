package com.marian.owncloudbackend.exceptions;

public class FileAlreadyOnFSException extends RuntimeException{
    public FileAlreadyOnFSException() {
    }

    public FileAlreadyOnFSException(String message) {
        super(message);
    }
}
