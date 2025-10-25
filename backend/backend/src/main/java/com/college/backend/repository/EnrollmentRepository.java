package com.college.backend.repository;

import com.college.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudentId(Long studentId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status);
    
    List<Enrollment> findByAcademicYear(String academicYear);
    
    List<Enrollment> findBySemester(Integer semester);
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.academicYear = :academicYear")
    List<Enrollment> findByStudentAndAcademicYear(@Param("studentId") Long studentId, @Param("academicYear") String academicYear);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.semester = :semester AND e.academicYear = :academicYear")
    List<Enrollment> findByStudentSemesterAndYear(@Param("studentId") Long studentId, @Param("semester") Integer semester, @Param("academicYear") String academicYear);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status")
    long countByCourseAndStatus(@Param("courseId") Long courseId, @Param("status") Enrollment.EnrollmentStatus status);
    
    @Query("SELECT e FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
    List<Enrollment> findByEnrollmentDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.department.id = :departmentId")
    List<Enrollment> findByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    List<Enrollment> findByInstructor(@Param("instructorId") Long instructorId);
    
    @Query("SELECT SUM(e.course.credits) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ENROLLED' AND e.academicYear = :academicYear AND e.semester = :semester")
    Integer getTotalCreditsForStudentInSemester(@Param("studentId") Long studentId, @Param("academicYear") String academicYear, @Param("semester") Integer semester);
    
    @Query("SELECT e FROM Enrollment e WHERE e.grade IS NULL AND e.status = 'ENROLLED'")
    List<Enrollment> findPendingGrades();
    
    @Query("SELECT e FROM Enrollment e WHERE e.grade IS NOT NULL")
    List<Enrollment> findCompletedEnrollments();
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.grade IS NOT NULL")
    List<Enrollment> findCompletedEnrollmentsByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT AVG(CASE WHEN e.grade = 'A+' THEN 10 WHEN e.grade = 'A' THEN 9 WHEN e.grade = 'B+' THEN 8 WHEN e.grade = 'B' THEN 7 WHEN e.grade = 'C+' THEN 6 WHEN e.grade = 'C' THEN 5 WHEN e.grade = 'D' THEN 4 ELSE 0 END) FROM Enrollment e WHERE e.student.id = :studentId AND e.grade IS NOT NULL")
    Double calculateGPAForStudent(@Param("studentId") Long studentId);
    
    // Additional missing methods
    long countByCourseId(Long courseId);
    
    long countByStudentId(Long studentId);
    
    List<Enrollment> findByCourseIdOrderByEnrollmentDateDesc(Long courseId);
    
    List<Enrollment> findByStudentIdOrderByEnrollmentDateDesc(Long studentId);
}