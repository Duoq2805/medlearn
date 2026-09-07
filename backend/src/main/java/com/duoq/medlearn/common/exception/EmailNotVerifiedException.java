package com.duoq.medlearn.common.exception;

public class EmailNotVerifiedException extends AuthenticationException {
    public EmailNotVerifiedException() {
        super("Please verify your email before logging in");
    }
}
