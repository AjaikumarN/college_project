package com.college.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
@Data
@EqualsAndHashCode(exclude = {"student", "course", "faculty"})
@ToString(exclude = {"student", "course", "faculty"})
public class AttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
    
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type")
    private SessionType sessionType = SessionType.THEORY;
    
    @Column(name = "session_number")
    private Integer sessionNumber; // Which session of the day (1, 2, 3, etc.)
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "academic_year")
    private String academicYear;
    
    @Column(name = "semester")
    private Integer semester;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "marked_by")
    private String markedBy; // Faculty who marked the attendance
    
    @Column(name = "marked_at")
    private LocalDateTime markedAt;
    
    @Column(name = "is_makeup_class")
    private Boolean isMakeupClass = false;
    
    @Column(name = "original_date")
    private LocalDate originalDate; // For makeup classes
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED, MEDICAL_LEAVE, ON_DUTY
    }
    
    public enum SessionType {
        THEORY, LAB, TUTORIAL, SEMINAR, PROJECT
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (markedAt == null) {
            markedAt = LocalDateTime.now();
        }
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Manual getters and setters for Lombok compatibility
    public AttendanceStatus getStatus() {
        return this.status;
    }
    
    public void setStatus(AttendanceStatus status) {
        this.status = status;
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
    
    public Faculty getFaculty() {
        return this.faculty;
    }
    
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }
    
    public LocalDate getAttendanceDate() {
        return this.attendanceDate;
    }
    
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }
}