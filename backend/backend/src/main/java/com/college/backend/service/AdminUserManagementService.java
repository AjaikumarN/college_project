package com.college.backend.service;

import com.college.backend.model.*;
import com.college.backend.repository.*;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AdminUserManagementService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    // Comprehensive User Management
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserManagementOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // User Statistics
        overview.put("totalUsers", userRepository.count());
        overview.put("activeUsers", userRepository.countByIsActive(true));
        overview.put("inactiveUsers", userRepository.countByIsActive(false));
        overview.put("verifiedUsers", userRepository.countByIsVerified(true));
        overview.put("unverifiedUsers", userRepository.countByIsVerified(false));
        
        // Role-based counts
        overview.put("totalStudents", userRepository.countByRole(User.UserRole.STUDENT));
        overview.put("totalFaculty", userRepository.countByRole(User.UserRole.FACULTY));
        overview.put("totalAdmins", userRepository.countByRole(User.UserRole.ADMIN));
        
        // Recent activity
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<User> recentUsers = userRepository.findByRegistrationDateAfter(weekAgo);
        overview.put("recentRegistrations", recentUsers.size());
        
        return overview;
    }

    // Advanced User Search and Filtering
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchUsersAdvanced(String searchTerm, User.UserRole role, Boolean isActive, 
                                         Boolean isVerified, LocalDateTime registrationStart, 
                                         LocalDateTime registrationEnd, Pageable pageable) {
        // This would typically involve a custom repository method or criteria query
        // For now, implementing basic search
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<User> searchResults = userRepository.searchUsers(searchTerm);
            // Apply additional filters
            return filterUsers(searchResults, role, isActive, isVerified, registrationStart, registrationEnd, pageable);
        }
        
        return userRepository.findAll(pageable);
    }

    // Bulk User Operations
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> performBulkUserOperation(List<Long> userIds, String operation) {
        Map<String, Object> result = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                
                switch (operation.toLowerCase()) {
                    case "activate":
                        user.setIsActive(true);
                        userRepository.save(user);
                        successful.add("User " + user.getEmail() + " activated");
                        break;
                    case "deactivate":
                        user.setIsActive(false);
                        userRepository.save(user);
                        successful.add("User " + user.getEmail() + " deactivated");
                        break;
                    case "verify":
                        user.setIsVerified(true);
                        userRepository.save(user);
                        successful.add("User " + user.getEmail() + " verified");
                        break;
                    case "delete":
                        userRepository.delete(user);
                        successful.add("User " + user.getEmail() + " deleted");
                        break;
                    default:
                        failed.add("Invalid operation for user: " + user.getEmail());
                }
            } catch (Exception e) {
                failed.add("Failed for user ID " + userId + ": " + e.getMessage());
            }
        }
        
        result.put("successful", successful);
        result.put("failed", failed);
        result.put("successCount", successful.size());
        result.put("failCount", failed.size());
        
        return result;
    }

    // Student Profile Management
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStudentManagementData() {
        Map<String, Object> data = new HashMap<>();
        
        // Student status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Student.StudentStatus status : Student.StudentStatus.values()) {
            statusDistribution.put(status.name(), studentRepository.countByStatus(status));
        }
        data.put("statusDistribution", statusDistribution);
        
        // Fee status distribution
        Map<String, Long> feeDistribution = new HashMap<>();
        for (Student.FeeStatus feeStatus : Student.FeeStatus.values()) {
            feeDistribution.put(feeStatus.name(), studentRepository.countByFeeStatus(feeStatus));
        }
        data.put("feeDistribution", feeDistribution);
        
        // Academic performance metrics
        List<Student> highPerformers = studentRepository.findByCgpaGreaterThanEqual(8.5);
        List<Student> lowPerformers = studentRepository.findByCgpaLessThan(5.0);
        
        data.put("highPerformers", highPerformers.size());
        data.put("lowPerformers", lowPerformers.size());
        data.put("hostelResidents", studentRepository.findByHostelResident(true).size());
        
        return data;
    }

    // Faculty Management Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getFacultyManagementData() {
        Map<String, Object> data = new HashMap<>();
        
        // Faculty status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Faculty.FacultyStatus status : Faculty.FacultyStatus.values()) {
            statusDistribution.put(status.name(), facultyRepository.countByStatus(status));
        }
        data.put("statusDistribution", statusDistribution);
        
        // Designation distribution
        Map<String, Long> designationDistribution = new HashMap<>();
        for (Faculty.Designation designation : Faculty.Designation.values()) {
            designationDistribution.put(designation.name(), facultyRepository.countByDesignation(designation));
        }
        data.put("designationDistribution", designationDistribution);
        
        // Experience analysis
        List<Faculty> experiencedFaculty = facultyRepository.findByExperienceYearsGreaterThanEqual(10);
        List<Faculty> juniorFaculty = facultyRepository.findByExperienceYearsLessThan(5);
        
        data.put("experiencedFaculty", experiencedFaculty.size());
        data.put("juniorFaculty", juniorFaculty.size());
        data.put("departmentHeads", facultyRepository.findByIsDepartmentHead(true).size());
        
        return data;
    }

    // User Activity Monitoring
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getUserActivityReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> activityReport = new ArrayList<>();
        
        // Recent logins
        List<User> recentlyActiveUsers = userRepository.findByLastLoginBetween(startDate, endDate);
        
        for (User user : recentlyActiveUsers) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("userId", user.getId());
            activity.put("name", user.getName());
            activity.put("email", user.getEmail());
            activity.put("role", user.getRole());
            activity.put("lastLogin", user.getLastLogin());
            activity.put("isActive", user.getIsActive());
            activity.put("isVerified", user.getIsVerified());
            
            activityReport.add(activity);
        }
        
        return activityReport;
    }

    // Role Migration and Upgrades
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> performRoleMigration(Long userId, User.UserRole newRole, Map<String, Object> additionalData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            User.UserRole oldRole = user.getRole();
            
            // Perform role-specific cleanup and setup
            switch (newRole) {
                case STUDENT:
                    if (oldRole != User.UserRole.STUDENT) {
                        // Create student profile if not exists
                        Optional<Student> existingStudent = studentRepository.findByUserId(userId);
                        if (existingStudent.isEmpty()) {
                            Student newStudent = createDefaultStudentProfile(user, additionalData);
                            studentRepository.save(newStudent);
                        }
                    }
                    break;
                    
                case FACULTY:
                    if (oldRole != User.UserRole.FACULTY) {
                        // Create faculty profile if not exists
                        Optional<Faculty> existingFaculty = facultyRepository.findByUserId(userId);
                        if (existingFaculty.isEmpty()) {
                            Faculty newFaculty = createDefaultFacultyProfile(user, additionalData);
                            facultyRepository.save(newFaculty);
                        }
                    }
                    break;
                    
                case ADMIN:
                    if (oldRole != User.UserRole.ADMIN) {
                        // Create admin profile if not exists
                        Optional<Admin> existingAdmin = adminRepository.findByUserId(userId);
                        if (existingAdmin.isEmpty()) {
                            Admin newAdmin = createDefaultAdminProfile(user, additionalData);
                            adminRepository.save(newAdmin);
                        }
                    }
                    break;
            }
            
            // Update user role
            user.setRole(newRole);
            userRepository.save(user);
            
            result.put("success", true);
            result.put("message", "User role successfully changed from " + oldRole + " to " + newRole);
            result.put("userId", userId);
            result.put("oldRole", oldRole);
            result.put("newRole", newRole);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Role migration failed: " + e.getMessage());
        }
        
        return result;
    }

    // Account Recovery and Reset
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> performAccountRecovery(Long userId, String operation) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            switch (operation.toLowerCase()) {
                case "reset_password":
                    // Generate temporary password
                    String tempPassword = generateTemporaryPassword();
                    user.setPassword(tempPassword); // Should be encoded
                    user.setIsActive(true);
                    userRepository.save(user);
                    result.put("temporaryPassword", tempPassword);
                    break;
                    
                case "unlock_account":
                    user.setIsActive(true);
                    user.setIsVerified(true);
                    userRepository.save(user);
                    break;
                    
                case "force_verification":
                    user.setIsVerified(true);
                    userRepository.save(user);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Invalid recovery operation: " + operation);
            }
            
            result.put("success", true);
            result.put("message", "Account recovery operation completed: " + operation);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Recovery failed: " + e.getMessage());
        }
        
        return result;
    }

    // Helper Methods
    private Page<User> filterUsers(List<User> users, User.UserRole role, Boolean isActive, 
                                  Boolean isVerified, LocalDateTime registrationStart, 
                                  LocalDateTime registrationEnd, Pageable pageable) {
        // Implementation for filtering - simplified version
        return Page.empty();
    }

    private Student createDefaultStudentProfile(User user, Map<String, Object> additionalData) {
        Student student = new Student();
        student.setUser(user);
        student.setStudentId(generateStudentId());
        student.setStatus(Student.StudentStatus.ACTIVE);
        student.setFeeStatus(Student.FeeStatus.PENDING);
        student.setHostelResident(false);
        student.setCgpa(0.0);
        
        // Set additional data if provided
        if (additionalData.containsKey("academicYear")) {
            student.setAcademicYear((String) additionalData.get("academicYear"));
        }
        if (additionalData.containsKey("semester")) {
            student.setSemester((Integer) additionalData.get("semester"));
        }
        
        return student;
    }

    private Faculty createDefaultFacultyProfile(User user, Map<String, Object> additionalData) {
        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setEmployeeId(generateEmployeeId());
        faculty.setStatus(Faculty.FacultyStatus.ACTIVE);
        faculty.setDesignation(Faculty.Designation.ASSISTANT_PROFESSOR);
        faculty.setEmploymentType(Faculty.EmploymentType.FULL_TIME);
        faculty.setIsDepartmentHead(false);
        faculty.setExperienceYears(0);
        
        return faculty;
    }

    private Admin createDefaultAdminProfile(User user, Map<String, Object> additionalData) {
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setAdminId(generateAdminId());
        admin.setEmployeeId(generateEmployeeId());
        admin.setStatus(Admin.AdminStatus.ACTIVE);
        admin.setAdminType(Admin.AdminType.ACADEMIC_ADMIN);
        admin.setAccessLevel(Admin.AccessLevel.DEPARTMENT);
        admin.setCanManageUsers(false);
        admin.setCanManageCourses(true);
        admin.setCanManageDepartments(false);
        admin.setDepartmentAccess("LIMITED");
        
        return admin;
    }

    private String generateStudentId() {
        return "STU" + System.currentTimeMillis();
    }

    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }

    private String generateAdminId() {
        return "ADM" + System.currentTimeMillis();
    }

    private String generateTemporaryPassword() {
        return "TempPass" + System.currentTimeMillis();
    }
}