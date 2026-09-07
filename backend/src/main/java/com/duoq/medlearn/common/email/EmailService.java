package com.duoq.medlearn.common.email;

public interface EmailService {
    void sendVerificationEmail(String to, String username, String token);
    void sendPasswordResetEmail(String to, String username, String token);
}
