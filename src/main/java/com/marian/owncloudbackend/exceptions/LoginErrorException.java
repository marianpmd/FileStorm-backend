package com.marian.owncloudbackend.exceptions;

public class LoginErrorException extends RuntimeException{
    public LoginErrorException() {
    }

    public LoginErrorException(String message) {
        super(message);
    }
}
