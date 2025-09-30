package com.college.backend.controller;

import com.college.backend.model.User;
import com.college.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User credentials) {
        Optional<User> optionalUser = userRepository.findByEmail(credentials.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email");
        }
        User user = optionalUser.get();
        if (!user.getPassword().equals(credentials.getPassword())) {
            return ResponseEntity.status(401).body("Incorrect password");
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/subjects")
    public ResponseEntity<?> updateSubjects(@PathVariable Long id, @RequestBody String subjectCodes) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optionalUser.get();
        user.setSelectedSubjects(subjectCodes);
        return ResponseEntity.ok(userRepository.save(user));
    }
}
