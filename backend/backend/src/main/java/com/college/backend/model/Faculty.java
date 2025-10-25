package com.college.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "faculty")
@Data
@EqualsAndHashCode(exclude = {"assignedCourses", "attendanceRecords"})
@ToString(exclude = {"assignedCourses", "attendanceRecords"})
public class Faculty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "faculty_id", unique = true, nullable = false)
    @NotBlank(message = "Faculty ID is required")
    private String facultyId;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Column(name = "employee_id", unique = true)
    private String employeeId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"courses", "students", "faculty"})
    private Department department;
    
    @Column(name = "designation")
    @Enumerated(EnumType.STRING)
    private Designation designation = Designation.LECTURER; // Professor, Associate Professor, Assistant Professor, Lecturer
    
    @Column(name = "qualification")
    private String qualification; // Ph.D, M.Tech, etc.
    
    @Column(name = "specialization")
    private String specialization;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "joining_date")
    private LocalDateTime joiningDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType = EmploymentType.PERMANENT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FacultyStatus status = FacultyStatus.ACTIVE;
    
    @Column(name = "office_room")
    private String officeRoom;
    
    @Column(name = "office_hours")
    private String officeHours;
    
    @Column(name = "research_interests", columnDefinition = "TEXT")
    private String researchInterests;
    
    @Column(name = "publications_count")
    private Integer publicationsCount = 0;
    
    @Column(name = "publications", columnDefinition = "TEXT")
    private String publications; // Added publications field
    
    @Column(name = "salary")
    private Double salary;
    
    @Column(name = "is_head_of_department")
    private Boolean isHeadOfDepartment = false;
    
    @Column(name = "is_department_head") // Alternative field name for compatibility
    private Boolean isDepartmentHead = false;
    
    @Column(name = "is_class_coordinator")
    private Boolean isClassCoordinator = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Course> assignedCourses;
    
    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<AttendanceRecord> attendanceRecords;
    
    public enum EmploymentType {
        PERMANENT, CONTRACT, VISITING, GUEST, FULL_TIME, PART_TIME
    }
    
    public enum FacultyStatus {
        ACTIVE, INACTIVE, ON_LEAVE, RETIRED, TERMINATED
    }
    
    public enum Designation {
        PROFESSOR, ASSOCIATE_PROFESSOR, ASSISTANT_PROFESSOR, LECTURER, HEAD_OF_DEPARTMENT
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
}