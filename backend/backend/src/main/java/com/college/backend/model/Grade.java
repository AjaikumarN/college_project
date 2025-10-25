package com.college.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Data
@EqualsAndHashCode(exclude = {"student", "course"})
@ToString(exclude = {"student", "course"})
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type")
    private AssessmentType assessmentType;
    
    @Column(name = "assessment_name")
    private String assessmentName; // e.g., "Internal Assessment 1", "Mid-term", "Final Exam"
    
    @Column(name = "max_marks")
    private Double maxMarks;
    
    @Column(name = "obtained_marks")
    private Double obtainedMarks;
    
    @Column(name = "marks_obtained") // Alternative field name for compatibility
    private Double marksObtained;
    
    @Column(name = "percentage")
    private Double percentage;
    
    @Column(name = "grade")
    private String grade; // A+, A, B+, B, C, D, F
    
    @Column(name = "grade_points")
    private Double gradePoints;
    
    @Column(name = "weightage")
    private Double weightage; // Weightage of this assessment in overall grade
    
    @Column(name = "academic_year")
    private String academicYear;
    
    @Column(name = "semester")
    private Integer semester;
    
    @Column(name = "exam_date")
    private LocalDateTime examDate;
    
    @Column(name = "graded_date")
    private LocalDateTime gradedDate;
    
    @Column(name = "result_date")
    private LocalDateTime resultDate;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "is_final_grade")
    private Boolean isFinalGrade = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "grade_type")
    private String gradeType;
    
    @Column(name = "letter_grade")
    private String letterGrade;
    
    @Column(name = "numeric_grade")
    private Double numericGrade;
    
    @Column(name = "max_points")
    private Double maxPoints;
    
    @Column(name = "points_earned")
    private Double pointsEarned;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    public enum AssessmentType {
        INTERNAL_ASSESSMENT_1, INTERNAL_ASSESSMENT_2, INTERNAL_ASSESSMENT_3,
        MID_TERM, FINAL_EXAM, ASSIGNMENT, PROJECT, LAB_WORK, PRESENTATION, QUIZ, VIVA
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculatePercentage();
        calculateGradePoints();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculatePercentage();
        calculateGradePoints();
    }
    
    private void calculatePercentage() {
        if (maxMarks != null && obtainedMarks != null && maxMarks > 0) {
            this.percentage = (obtainedMarks / maxMarks) * 100;
        }
    }
    
    private void calculateGradePoints() {
        if (percentage != null) {
            if (percentage >= 90) {
                this.grade = "A+";
                this.gradePoints = 10.0;
            } else if (percentage >= 80) {
                this.grade = "A";
                this.gradePoints = 9.0;
            } else if (percentage >= 70) {
                this.grade = "B+";
                this.gradePoints = 8.0;
            } else if (percentage >= 60) {
                this.grade = "B";
                this.gradePoints = 7.0;
            } else if (percentage >= 50) {
                this.grade = "C";
                this.gradePoints = 6.0;
            } else if (percentage >= 40) {
                this.grade = "D";
                this.gradePoints = 5.0;
            } else {
                this.grade = "F";
                this.gradePoints = 0.0;
            }
        }
    }
    
    // Manual getters and setters for Lombok compatibility
    public LocalDateTime getGradeDate() {
        return this.gradedDate;
    }
    
    public void setGradeDate(LocalDateTime gradeDate) {
        this.gradedDate = gradeDate;
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
    
    public Double getMarksObtained() {
        return this.marksObtained;
    }
    
    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }
}