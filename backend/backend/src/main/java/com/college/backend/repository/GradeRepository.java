package com.college.backend.repository;

import com.college.backend.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    List<Grade> findByStudentId(Long studentId);
    
    List<Grade> findByCourseId(Long courseId);
    
    List<Grade> findByAssessmentType(Grade.AssessmentType assessmentType);
    
    List<Grade> findByAcademicYear(String academicYear);
    
    List<Grade> findBySemester(Integer semester);
    
    Optional<Grade> findByStudentIdAndCourseIdAndAssessmentType(Long studentId, Long courseId, Grade.AssessmentType assessmentType);
    
    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.academicYear = :academicYear")
    List<Grade> findByStudentAndAcademicYear(@Param("studentId") Long studentId, @Param("academicYear") String academicYear);
    
    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.semester = :semester AND g.academicYear = :academicYear")
    List<Grade> findByStudentSemesterAndYear(@Param("studentId") Long studentId, @Param("semester") Integer semester, @Param("academicYear") String academicYear);
    
    @Query("SELECT g FROM Grade g WHERE g.course.id = :courseId AND g.assessmentType = :assessmentType")
    List<Grade> findByCourseAndAssessmentType(@Param("courseId") Long courseId, @Param("assessmentType") Grade.AssessmentType assessmentType);
    
    @Query("SELECT g FROM Grade g WHERE g.marksObtained >= :minMarks")
    List<Grade> findByMinimumMarks(@Param("minMarks") Double minMarks);
    
    @Query("SELECT g FROM Grade g WHERE g.marksObtained BETWEEN :minMarks AND :maxMarks")
    List<Grade> findByMarksRange(@Param("minMarks") Double minMarks, @Param("maxMarks") Double maxMarks);
    
    @Query("SELECT AVG(g.marksObtained) FROM Grade g WHERE g.course.id = :courseId AND g.assessmentType = :assessmentType")
    Double getAverageMarksByCourseAndAssessment(@Param("courseId") Long courseId, @Param("assessmentType") Grade.AssessmentType assessmentType);
    
    @Query("SELECT MAX(g.marksObtained) FROM Grade g WHERE g.course.id = :courseId AND g.assessmentType = :assessmentType")
    Double getMaxMarksByCourseAndAssessment(@Param("courseId") Long courseId, @Param("assessmentType") Grade.AssessmentType assessmentType);
    
    @Query("SELECT MIN(g.marksObtained) FROM Grade g WHERE g.course.id = :courseId AND g.assessmentType = :assessmentType")
    Double getMinMarksByCourseAndAssessment(@Param("courseId") Long courseId, @Param("assessmentType") Grade.AssessmentType assessmentType);
    
    @Query("SELECT g FROM Grade g WHERE g.gradedDate BETWEEN :startDate AND :endDate")
    List<Grade> findByGradedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT g FROM Grade g WHERE g.course.instructor.id = :instructorId")
    List<Grade> findByInstructor(@Param("instructorId") Long instructorId);
    
    @Query("SELECT g FROM Grade g WHERE g.course.department.id = :departmentId")
    List<Grade> findByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.marksObtained >= :passingMarks AND g.course.id = :courseId")
    long countPassingGradesByCourse(@Param("courseId") Long courseId, @Param("passingMarks") Double passingMarks);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.marksObtained < :passingMarks AND g.course.id = :courseId")
    long countFailingGradesByCourse(@Param("courseId") Long courseId, @Param("passingMarks") Double passingMarks);
    
    @Query("SELECT AVG(g.marksObtained) FROM Grade g WHERE g.student.id = :studentId")
    Double getOverallAverageForStudent(@Param("studentId") Long studentId);
    
    // Added missing method
    @Query("SELECT AVG(g.gradePoints) FROM Grade g WHERE g.student.id = :studentId")
    Double calculateGPAForStudent(@Param("studentId") Long studentId);
    
    // Additional missing methods
    List<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT g FROM Grade g WHERE g.course.id = :courseId AND g.gradedDate BETWEEN :startDate AND :endDate")
    List<Grade> findByCourseIdAndGradedDateBetween(@Param("courseId") Long courseId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Additional missing methods
    List<Grade> findByCourseIdOrderByGradedDateDesc(Long courseId);
    
    List<Grade> findByCourseIdOrderByGradedDateAsc(Long courseId);
    
    List<Grade> findByStudentIdAndCourseIdOrderByGradedDateDesc(Long studentId, Long courseId);
    
    List<Grade> findByStudentIdAndCourseIdOrderByGradedDateAsc(Long studentId, Long courseId);
    
    List<Grade> findByStudentIdOrderByGradedDateDesc(Long studentId);
}