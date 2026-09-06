package com.duoq.medlearn.service;

public interface EmailService {
    void sendVerificationEmail(String to, String username, String token);
    void sendPasswordResetEmail(String to, String username, String token);
}
