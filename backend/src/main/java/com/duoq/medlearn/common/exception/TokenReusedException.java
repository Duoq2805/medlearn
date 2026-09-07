package com.duoq.medlearn.common.exception;

public class TokenReusedException extends AuthenticationException {
    public TokenReusedException() {
        super("Security violation: token reuse detected");
    }
}
