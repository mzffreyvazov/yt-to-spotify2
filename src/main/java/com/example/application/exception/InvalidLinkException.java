package com.example.application.exception;


public class InvalidLinkException extends RuntimeException {
    public InvalidLinkException(String message) {
        super(message);
    }

    public InvalidLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
