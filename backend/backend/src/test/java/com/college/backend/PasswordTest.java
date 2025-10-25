package com.college.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    
    @Test
    public void testPasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Romlg/.k0E9qmdLcHiJlgW3K";
        String facultyHash = "$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW";
        String studentHash = "$2a$10$IQvE3Z9l0BULj5B8YOqp5OKZLMcNxGCFBFAT7mKL1Z.wGvL9zYBwS";
        
        System.out.println("Testing admin123 against adminHash: " + encoder.matches("admin123", adminHash));
        System.out.println("Testing faculty123 against facultyHash: " + encoder.matches("faculty123", facultyHash));
        System.out.println("Testing student123 against studentHash: " + encoder.matches("student123", studentHash));
        
        // Generate fresh hashes
        System.out.println("\nFresh BCrypt hashes:");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("faculty123: " + encoder.encode("faculty123"));
        System.out.println("student123: " + encoder.encode("student123"));
    }
}
