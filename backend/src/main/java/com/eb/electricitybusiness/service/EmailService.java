package com.eb.electricitybusiness.service;

public interface EmailService {
    void sendVerificationEmail(String to, String name, String verificationCode);
    void sendWelcomeEmail(String to, String name);
}
