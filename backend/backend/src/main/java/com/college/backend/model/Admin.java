package com.college.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(exclude = {"permissions"})
@ToString(exclude = {"permissions"})
public class Admin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "admin_id", unique = true, nullable = false)
    @NotBlank(message = "Admin ID is required")
    private String adminId;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Column(name = "employee_id", unique = true)
    private String employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_type")
    private AdminType adminType = AdminType.GENERAL;
    
    @Column(name = "designation")
    private String designation; // Registrar, Academic Officer, IT Administrator, etc.
    
    @Column(name = "department_access")
    private String departmentAccess; // ALL or specific department codes separated by comma
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    private AccessLevel accessLevel = AccessLevel.DEPARTMENT;
    
    @Column(name = "joining_date")
    private LocalDateTime joiningDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdminStatus status = AdminStatus.ACTIVE;
    
    @Column(name = "office_location")
    private String officeLocation;
    
    @Column(name = "working_hours")
    private String workingHours;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "login_count")
    private Integer loginCount = 0;
    
    @Column(name = "can_manage_users")
    private Boolean canManageUsers = false;
    
    @Column(name = "can_manage_courses")
    private Boolean canManageCourses = false;
    
    @Column(name = "can_manage_departments")
    private Boolean canManageDepartments = false;
    
    @Column(name = "can_view_reports")
    private Boolean canViewReports = true;
    
    @Column(name = "can_manage_fees")
    private Boolean canManageFees = false;
    
    @Column(name = "can_manage_attendance")
    private Boolean canManageAttendance = false;
    
    @Column(name = "can_manage_grades")
    private Boolean canManageGrades = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    private List<String> permissions;
    
    public enum AdminType {
        SUPER_ADMIN, ACADEMIC_ADMIN, IT_ADMIN, FINANCE_ADMIN, GENERAL
    }
    
    public enum AccessLevel {
        SYSTEM, INSTITUTION, DEPARTMENT, LIMITED
    }
    
    public enum AdminStatus {
        ACTIVE, INACTIVE, SUSPENDED, TERMINATED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joiningDate == null) {
            joiningDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for permission checking
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    public boolean isSuperAdmin() {
        return adminType == AdminType.SUPER_ADMIN;
    }
    
    public boolean canAccessDepartment(String departmentCode) {
        if (accessLevel == AccessLevel.SYSTEM || accessLevel == AccessLevel.INSTITUTION) {
            return true;
        }
        if (departmentAccess == null || departmentAccess.equals("ALL")) {
            return true;
        }
        return departmentAccess.contains(departmentCode);
    }
}