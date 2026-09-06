package com.duoq.medlearn.exception;

public class TokenReusedException extends AuthenticationException {
    public TokenReusedException() {
        super("Security violation: token reuse detected");
    }
}
