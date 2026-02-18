package com.example.application.model.exception;

public class ErrorResponse {
    private String error;
    private String message;
    private String timestamp;
    private int status;
    private String path;

    public ErrorResponse(String error, String message, String timestamp, int status, String path) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
