package com.college.backend.service;

import com.college.backend.dto.LoginRequest;
import com.college.backend.dto.LoginResponse;
import com.college.backend.dto.RegisterRequest;
import com.college.backend.exception.InvalidCredentialsException;
import com.college.backend.exception.UserAlreadyExistsException;
import com.college.backend.model.User;
import com.college.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public LoginResponse login(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is not activated. Please check your email for verification instructions.");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponse(user);
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    public User register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists. Please use a different email address.");
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setDob(registerRequest.getDob());
        user.setGender(registerRequest.getGender());
        user.setCourse(registerRequest.getCourse());
        user.setYear(registerRequest.getYear());
        user.setSemester(registerRequest.getSemester());
        user.setStudentId(registerRequest.getStudentId());
        user.setEmailUpdates(registerRequest.getEmailUpdates());
        user.setRole(User.UserRole.STUDENT);
        user.setIsActive(true); // Set to true for immediate activation
        user.setIsVerified(true); // Set to true for immediate verification
        user.setRegistrationDate(LocalDateTime.now());
        
        // Generate verification token (for future use)
        user.setVerificationToken(UUID.randomUUID().toString());

        // Save user
        User savedUser = userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return savedUser;
    }

    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return false;
        }

        user.setIsVerified(true);
        user.setIsActive(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return true;
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.getIsVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Generate new token if needed
        if (user.getVerificationToken() == null) {
            user.setVerificationToken(UUID.randomUUID().toString());
            userRepository.save(user);
        }

        emailService.sendVerificationEmail(user);
    }
}