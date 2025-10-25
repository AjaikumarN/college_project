package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.dto.LoginResponse;
import com.college.backend.dto.RegisterRequest;
import com.college.backend.model.User;
import com.college.backend.repository.UserRepository;
import com.college.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User credentials) {
        Optional<User> optionalUser = userRepository.findByEmail(credentials.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid email"));
        }
        User user = optionalUser.get();
        
        // Check password with encryption support
        boolean passwordMatches = false;
        if (user.getPassword().startsWith("$2a$")) {
            // Password is encrypted
            passwordMatches = passwordEncoder.matches(credentials.getPassword(), user.getPassword());
        } else {
            // Password is plain text (for backward compatibility)
            passwordMatches = user.getPassword().equals(credentials.getPassword());
        }
        
        if (!passwordMatches) {
            return ResponseEntity.status(401).body(ApiResponse.error("Incorrect password"));
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        return ResponseEntity.ok(new LoginResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // Set the full name if not provided
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                registerRequest.setName(registerRequest.getFirstName() + " " + registerRequest.getLastName());
            }
            
            User user = authService.register(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("Registration successful!", new LoginResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(new LoginResponse(user.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(new LoginResponse(user.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/subjects")
    public ResponseEntity<?> updateSubjects(@PathVariable Long id, @RequestBody String subjectCodes) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        user.setSelectedSubjects(subjectCodes);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(new LoginResponse(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = optionalUser.get();
        
        // Update fields
        if (userUpdate.getName() != null) existingUser.setName(userUpdate.getName());
        if (userUpdate.getPhone() != null) existingUser.setPhone(userUpdate.getPhone());
        if (userUpdate.getCourse() != null) existingUser.setCourse(userUpdate.getCourse());
        if (userUpdate.getYear() != null) existingUser.setYear(userUpdate.getYear());
        if (userUpdate.getSemester() != null) existingUser.setSemester(userUpdate.getSemester());
        if (userUpdate.getGender() != null) existingUser.setGender(userUpdate.getGender());
        if (userUpdate.getDob() != null) existingUser.setDob(userUpdate.getDob());
        if (userUpdate.getSelectedSubjects() != null) existingUser.setSelectedSubjects(userUpdate.getSelectedSubjects());
        
        User savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(new LoginResponse(savedUser));
    }
}
