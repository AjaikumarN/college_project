package com.college.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
@EqualsAndHashCode(exclude = {"courses", "faculty"})
@ToString(exclude = {"courses", "faculty"})
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Department code is required")
    private String departmentCode; // e.g., "CSE", "ECE", "MECH"
    
    @Column(nullable = false)
    @NotBlank(message = "Department name is required")
    private String departmentName; // e.g., "Computer Science Engineering"
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_department_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Faculty headOfDepartment; // Changed from String to Faculty object
    
    @Column(name = "established_year")
    private Integer establishedYear;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Course> courses;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Faculty> faculty;
    
    public enum DepartmentStatus {
        ACTIVE, INACTIVE, UNDER_REVIEW, CLOSED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Manual getters for compatibility
    public String getCode() {
        return this.departmentCode;
    }
    
    public String getName() {
        return this.departmentName;
    }
    
    public boolean getIsActive() {
        return this.status == DepartmentStatus.ACTIVE;
    }
    
    public void setIsActive(boolean isActive) {
        this.status = isActive ? DepartmentStatus.ACTIVE : DepartmentStatus.INACTIVE;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setName(String name) {
        this.departmentName = name;
    }
}