package com.college.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@EqualsAndHashCode(exclude = {"enrollments", "attendanceRecords", "grades"})
@ToString(exclude = {"enrollments", "attendanceRecords", "grades"})
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_code", unique = true, nullable = false)
    @NotBlank(message = "Course code is required")
    private String courseCode; // e.g., "CSE101", "MATH201"
    
    @Column(name = "course_name", nullable = false)
    @NotBlank(message = "Course name is required")
    private String courseName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "credits")
    @NotNull(message = "Credits are required")
    private Integer credits;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty instructor; // Changed from faculty to instructor
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CourseStatus status = CourseStatus.ACTIVE;
    
    @Column(name = "semester")
    private Integer semester; // 1-8
    
    @Column(name = "academic_year")
    private String academicYear; // e.g., "2023-2024"
    
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type")
    private CourseType type = CourseType.CORE; // Added getter method name compatibility
    
    @Column(name = "max_students")
    private Integer maxStudents = 60;
    
    @Column(name = "enrollment_limit") // Alias for maxStudents for backward compatibility
    private Integer enrollmentLimit = 60;
    
    @Column(name = "enrolled_students")
    private Integer enrolledStudents = 0;
    
    @Column(name = "max_capacity") // Another alias for enrollment limit
    private Integer maxCapacity = 60;
    
    @Column(name = "theory_hours")
    private Integer theoryHours;
    
    @Column(name = "lab_hours")
    private Integer labHours;
    
    @Column(name = "tutorial_hours")
    private Integer tutorialHours;
    
    @Column(name = "schedule")
    private String schedule; // e.g., "MON-09:00,WED-10:00,FRI-11:00"
    
    @Column(name = "classroom")
    private String classroom;
    
    @Column(name = "lab_name")
    private String labName;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "syllabus", columnDefinition = "TEXT")
    private String syllabus;
    
    @Column(name = "prerequisites")
    private String prerequisites; // Comma-separated course codes
    
    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;
    
    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;
    
    @Column(name = "assessment_pattern")
    private String assessmentPattern; // e.g., "IA1:20,IA2:20,SEE:60"
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AttendanceRecord> attendanceRecords;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Grade> grades;
    
    public enum CourseType {
        CORE, ELECTIVE, OPEN_ELECTIVE, MANDATORY, AUDIT
    }
    
    public enum CourseStatus {
        ACTIVE, INACTIVE, COMPLETED, ARCHIVED, CANCELLED, DRAFT
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
    
    // Helper methods
    public Integer getTotalHours() {
        int total = 0;
        if (theoryHours != null) total += theoryHours;
        if (labHours != null) total += labHours;
        if (tutorialHours != null) total += tutorialHours;
        return total;
    }
    
    public boolean hasAvailableSlots() {
        if (maxStudents == null) return true;
        return enrolledStudents < maxStudents;
    }
    
    public Integer getAvailableSlots() {
        if (maxStudents == null) return 999; // Return a large number if no limit
        return maxStudents - enrolledStudents;
    }
    
    // Manual getters and setters for Lombok compatibility
    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }
    
    public String getObjectives() {
        return this.objectives;
    }
}
