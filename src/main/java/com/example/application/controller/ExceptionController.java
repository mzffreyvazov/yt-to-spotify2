package com.example.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.application.exception.InvalidLinkException;
import com.example.application.exception.UpstreamServiceException;
import com.example.application.model.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(InvalidLinkException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLinkException(InvalidLinkException ex, HttpServletRequest request) {
        System.err.println("Invalid link error: " + ex.getMessage());
        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_LINK",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamServiceException(UpstreamServiceException ex,
                                                                        HttpServletRequest request) {
        System.err.println("Upstream service error: " + ex.getMessage());
        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "UPSTREAM_UNAVAILABLE",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        System.err.println("Unexpected error: " + ex.getMessage());
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Unexpected internal error",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status,
                                                     String errorCode,
                                                     String message,
                                                     String path) {
        String timestamp = java.time.Instant.now().toString();
        ErrorResponse body = new ErrorResponse(
                errorCode,
                message,
                timestamp,
                status.value(),
                path
        );
        return ResponseEntity.status(status).body(body);
    }
    
}
