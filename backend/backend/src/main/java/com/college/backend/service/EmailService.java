package com.college.backend.service;

import com.college.backend.model.User;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendVerificationEmail(User user) {
        // In a real application, you would integrate with an email service
        // like SendGrid, AWS SES, or JavaMail
        
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken();
        
        System.out.println("=== EMAIL VERIFICATION ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Verify Your Karunya College Account");
        System.out.println("Body:");
        System.out.println("Dear " + user.getName() + ",");
        System.out.println("");
        System.out.println("Welcome to Karunya College Portal!");
        System.out.println("");
        System.out.println("Please click the following link to verify your email address:");
        System.out.println(verificationLink);
        System.out.println("");
        System.out.println("If you didn't create this account, please ignore this email.");
        System.out.println("");
        System.out.println("Best regards,");
        System.out.println("Karunya College IT Team");
        System.out.println("========================");
    }

    public void sendWelcomeEmail(User user) {
        System.out.println("=== WELCOME EMAIL ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Welcome to Karunya College Portal");
        System.out.println("Body: Welcome " + user.getName() + "! Your account has been created successfully.");
        System.out.println("Course: " + user.getCourse());
        System.out.println("Year: " + user.getYear());
        System.out.println("====================");
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        
        System.out.println("=== PASSWORD RESET EMAIL ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: Reset Your Password");
        System.out.println("Body: Click here to reset your password: " + resetLink);
        System.out.println("===========================");
    }
}