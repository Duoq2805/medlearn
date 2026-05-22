package com.duoq.medlearn.exception;

public class AccountDeactivatedException extends AuthenticationException {
    public AccountDeactivatedException() {
        super("Account has been deactivated");
    }
}
