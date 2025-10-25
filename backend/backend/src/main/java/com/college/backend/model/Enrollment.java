package com.college.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@EqualsAndHashCode(exclude = {"student", "course"})
@ToString(exclude = {"student", "course"})
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;
    
    @Column(name = "academic_year")
    private String academicYear;
    
    @Column(name = "semester")
    private Integer semester;
    
    @Column(name = "grade")
    private String grade; // Final grade for the course
    
    @Column(name = "grade_points")
    private Double gradePoints;
    
    @Column(name = "is_repeat")
    private Boolean isRepeat = false;
    
    @Column(name = "attendance_percentage")
    private Double attendancePercentage = 0.0;
    
    @Column(name = "dropped_date")
    private LocalDateTime droppedDate;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EnrollmentStatus {
        ENROLLED, COMPLETED, DROPPED, FAILED, WITHDRAWN
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Manual getters and setters for Lombok compatibility  
    public String getCurrentGrade() {
        return this.grade;
    }
    
    public void setCurrentGrade(String grade) {
        this.grade = grade;
    }
    
    public String getFinalGrade() {
        return this.grade;
    }
    
    public void setFinalGrade(String finalGrade) {
        this.grade = finalGrade;
    }
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Student getStudent() {
        return this.student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Course getCourse() {
        return this.course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
    }
    
    public LocalDateTime getEnrollmentDate() {
        return this.enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}