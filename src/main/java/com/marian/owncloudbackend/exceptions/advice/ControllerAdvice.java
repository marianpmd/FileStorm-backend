package com.marian.owncloudbackend.exceptions.advice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.marian.owncloudbackend.exceptions.AbnormalAssignmentAmountException;
import com.marian.owncloudbackend.exceptions.DirectoryNotFoundException;
import com.marian.owncloudbackend.exceptions.FileAlreadyOnFSException;
import com.marian.owncloudbackend.exceptions.FileEntityNotFoundException;
import com.marian.owncloudbackend.exceptions.FileIsNotPublicException;
import com.marian.owncloudbackend.exceptions.LoginErrorException;
import com.marian.owncloudbackend.exceptions.OutOfSpaceException;
import com.marian.owncloudbackend.exceptions.UserAlreadyExistsException;

@RestControllerAdvice
public class ControllerAdvice {

    public static final String TIMESTAMP_LABEL = "timestamp";
    public static final String MESSAGE_LABEL = "message";

    @ExceptionHandler(LoginErrorException.class)
    public ResponseEntity<Object> loginFailed(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage("User already exists", HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(FileEntityNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundInDB(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileAlreadyOnFSException.class)
    public ResponseEntity<Object> handleFileAlreadyExists(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<Object> handleDirectoryNotFound(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Object> getResponseEntityWithTimestampAndMessage(String ex, HttpStatus notFound) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_LABEL, LocalDateTime.now());
        body.put(MESSAGE_LABEL, ex);

        return new ResponseEntity<>(body, notFound);
    }

    @ExceptionHandler(OutOfSpaceException.class)
    public ResponseEntity<Object> handleOutOfSpace(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(AbnormalAssignmentAmountException.class)
    public ResponseEntity<Object> handleAbnormalAmount(
            RuntimeException ex, WebRequest request) {

        return getResponseEntityWithTimestampAndMessage(ex.getMessage(), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(FileIsNotPublicException.class)
    public String handleFileNotPublic(RuntimeException ex, WebRequest request) {
        return ex.getMessage();
    }

}
