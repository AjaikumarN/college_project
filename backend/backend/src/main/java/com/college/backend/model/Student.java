package com.college.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@EqualsAndHashCode(exclude = {"enrollments", "attendanceRecords", "grades"})
@ToString(exclude = {"enrollments", "attendanceRecords", "grades"})
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "student_id", unique = true, nullable = false)
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "semester")
    private Integer semester;
    
    @Column(name = "hostel_resident")
    private Boolean hostelResident = false;
    
    @Column(name = "admission_number", unique = true)
    private String admissionNumber;
    
    @Column(name = "roll_number")
    private String rollNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"courses", "students"})
    private Department department;
    
    @Column(name = "course")
    private String course;
    
    @Column(name = "academic_year")
    private String academicYear; // e.g., "2023-2024"
    
    @Column(name = "current_year")
    private Integer currentYear; // 1, 2, 3, 4
    
    @Column(name = "current_semester")
    private Integer currentSemester; // 1-8
    
    @Column(name = "admission_date")
    private LocalDateTime admissionDate;
    
    @Column(name = "graduation_date")
    private LocalDateTime graduationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StudentStatus status = StudentStatus.ACTIVE;
    
    @Column(name = "cgpa")
    private Double cgpa;
    
    @Column(name = "total_credits")
    private Integer totalCredits = 0;
    
    @Column(name = "parent_name")
    private String parentName;
    
    @Column(name = "parent_phone")
    @Pattern(regexp = "^[+]?[1-9][\\d]{0,15}$", message = "Please provide a valid parent phone number")
    private String parentPhone;
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "blood_group")
    private String bloodGroup;
    
    @Column(name = "is_hostel_student")
    private Boolean isHostelStudent = false;
    
    @Column(name = "hostel_room_number")
    private String hostelRoomNumber;
    
    @Column(name = "fee_status")
    @Enumerated(EnumType.STRING)
    private FeeStatus feeStatus = FeeStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<AttendanceRecord> attendanceRecords;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Grade> grades;
    
    public enum StudentStatus {
        ACTIVE, INACTIVE, GRADUATED, SUSPENDED, TRANSFERRED
    }
    
    public enum FeeStatus {
        PAID, PENDING, PARTIAL, OVERDUE
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (admissionDate == null) {
            admissionDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}