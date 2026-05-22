package com.duoq.medlearn.exception;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
