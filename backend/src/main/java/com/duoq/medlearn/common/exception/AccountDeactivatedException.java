package com.duoq.medlearn.common.exception;

public class AccountDeactivatedException extends AuthenticationException {
    public AccountDeactivatedException() {
        super("Account has been deactivated");
    }
}
