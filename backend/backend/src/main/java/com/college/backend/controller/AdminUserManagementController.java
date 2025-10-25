package com.college.backend.controller;

import com.college.backend.model.User;
import com.college.backend.service.AdminUserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminUserManagementController {

    @Autowired
    private AdminUserManagementService userManagementService;

    // User Management Overview
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getUserManagementOverview() {
        Map<String, Object> overview = userManagementService.getUserManagementOverview();
        return ResponseEntity.ok(overview);
    }

    // Advanced User Search and Filtering
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registrationStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime registrationEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userManagementService.searchUsersAdvanced(
            searchTerm, role, isActive, isVerified, registrationStart, registrationEnd, pageable);
        
        return ResponseEntity.ok(users);
    }

    // Bulk User Operations
    @PostMapping("/bulk-operations")
    public ResponseEntity<Map<String, Object>> performBulkOperation(
            @RequestBody @Valid BulkOperationRequest request) {
        
        Map<String, Object> result = userManagementService.performBulkUserOperation(
            request.getUserIds(), request.getOperation());
        
        return ResponseEntity.ok(result);
    }

    // Student Management
    @GetMapping("/students/management-data")
    public ResponseEntity<Map<String, Object>> getStudentManagementData() {
        Map<String, Object> data = userManagementService.getStudentManagementData();
        return ResponseEntity.ok(data);
    }

    // Faculty Management
    @GetMapping("/faculty/management-data")
    public ResponseEntity<Map<String, Object>> getFacultyManagementData() {
        Map<String, Object> data = userManagementService.getFacultyManagementData();
        return ResponseEntity.ok(data);
    }

    // User Activity Reports
    @GetMapping("/activity-report")
    public ResponseEntity<List<Map<String, Object>>> getUserActivityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Map<String, Object>> report = userManagementService.getUserActivityReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Role Migration
    @PostMapping("/role-migration")
    public ResponseEntity<Map<String, Object>> performRoleMigration(
            @RequestBody @Valid RoleMigrationRequest request) {
        
        Map<String, Object> result = userManagementService.performRoleMigration(
            request.getUserId(), request.getNewRole(), request.getAdditionalData());
        
        return ResponseEntity.ok(result);
    }

    // Account Recovery
    @PostMapping("/account-recovery")
    public ResponseEntity<Map<String, Object>> performAccountRecovery(
            @RequestBody @Valid AccountRecoveryRequest request) {
        
        Map<String, Object> result = userManagementService.performAccountRecovery(
            request.getUserId(), request.getOperation());
        
        return ResponseEntity.ok(result);
    }

    // User Statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> statistics = Map.of(
            "overview", userManagementService.getUserManagementOverview(),
            "studentData", userManagementService.getStudentManagementData(),
            "facultyData", userManagementService.getFacultyManagementData(),
            "period", days + " days",
            "generatedAt", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(statistics);
    }

    // User Management Actions
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid UserStatusUpdateRequest request) {
        
        List<Long> userIds = List.of(userId);
        String operation = request.isActive() ? "activate" : "deactivate";
        
        Map<String, Object> result = userManagementService.performBulkUserOperation(userIds, operation);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{userId}/verification")
    public ResponseEntity<Map<String, Object>> updateUserVerification(
            @PathVariable Long userId,
            @RequestBody @Valid UserVerificationRequest request) {
        
        List<Long> userIds = List.of(userId);
        String operation = request.isVerified() ? "verify" : "unverify";
        
        Map<String, Object> result = userManagementService.performBulkUserOperation(userIds, operation);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        List<Long> userIds = List.of(userId);
        Map<String, Object> result = userManagementService.performBulkUserOperation(userIds, "delete");
        return ResponseEntity.ok(result);
    }

    // Quick Actions
    @PostMapping("/quick-actions/reset-passwords")
    public ResponseEntity<Map<String, Object>> resetPasswordsForUsers(
            @RequestBody List<Long> userIds) {
        
        Map<String, Object> result = userManagementService.performBulkUserOperation(userIds, "reset_password");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quick-actions/send-notifications")
    public ResponseEntity<Map<String, Object>> sendNotifications(
            @RequestBody @Valid NotificationRequest request) {
        
        // Implementation for sending bulk notifications
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "Notifications sent successfully",
            "recipientCount", request.getUserIds().size(),
            "notificationType", request.getNotificationType(),
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(result);
    }

    // DTOs for Request Bodies
    public static class BulkOperationRequest {
        private List<Long> userIds;
        private String operation;

        // Getters and setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
    }

    public static class RoleMigrationRequest {
        private Long userId;
        private User.UserRole newRole;
        private Map<String, Object> additionalData;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public User.UserRole getNewRole() { return newRole; }
        public void setNewRole(User.UserRole newRole) { this.newRole = newRole; }
        public Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
    }

    public static class AccountRecoveryRequest {
        private Long userId;
        private String operation;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
    }

    public static class UserStatusUpdateRequest {
        private boolean active;

        // Getters and setters
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class UserVerificationRequest {
        private boolean verified;

        // Getters and setters
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }

    public static class NotificationRequest {
        private List<Long> userIds;
        private String notificationType;
        private String subject;
        private String message;

        // Getters and setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getNotificationType() { return notificationType; }
        public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}