package com.example.application.exception;

public class InternalProcessingException extends RuntimeException {
    public InternalProcessingException(String message) {
        super(message);
    }

    public InternalProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
