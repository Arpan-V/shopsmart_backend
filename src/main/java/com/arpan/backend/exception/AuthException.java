package com.arpan.backend.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}