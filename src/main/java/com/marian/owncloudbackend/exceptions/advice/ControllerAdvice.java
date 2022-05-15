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
import com.marian.owncloudbackend.exceptions.LoginErrorException;
import com.marian.owncloudbackend.exceptions.OutOfSpaceException;
import com.marian.owncloudbackend.exceptions.UserAlreadyExistsException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(LoginErrorException.class)
    public ResponseEntity<Object> loginFailed(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "User already exists");

        return new ResponseEntity<>(body, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(FileEntityNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundInDB(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileAlreadyOnFSException.class)
    public ResponseEntity<Object> handleFileAlreadyExists(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<Object> handleDirectoryNotFound(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OutOfSpaceException.class)
    public ResponseEntity<Object> handleOutOfSpace(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(AbnormalAssignmentAmountException.class)
    public ResponseEntity<Object> handleAbnormalAmount(
            RuntimeException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.PRECONDITION_FAILED);
    }

}
