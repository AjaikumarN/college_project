package com.college.backend.repository;

import com.college.backend.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository 
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    
    List<AttendanceRecord> findByStudentId(Long studentId);
    
    List<AttendanceRecord> findByCourseId(Long courseId);
    
    List<AttendanceRecord> findByFacultyId(Long facultyId);
    
    List<AttendanceRecord> findByAttendanceDate(LocalDate attendanceDate);
    
    List<AttendanceRecord> findByStatus(AttendanceRecord.AttendanceStatus status);
    
    List<AttendanceRecord> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<AttendanceRecord> findByCourseIdAndAttendanceDate(Long courseId, LocalDate attendanceDate);
    
    List<AttendanceRecord> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);
    
    List<AttendanceRecord> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<AttendanceRecord> findByCourseIdAndAttendanceDateBetween(Long courseId, LocalDate startDate, LocalDate endDate);
    
    List<AttendanceRecord> findByStudentIdAndAttendanceDateBetween(Long studentId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.course.id = :courseId AND ar.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByCourseIdAndAttendanceDateRange(@Param("courseId") Long courseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") AttendanceRecord.AttendanceStatus status);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.course.id = :courseId AND ar.status = :status")
    long countByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") AttendanceRecord.AttendanceStatus status);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.course.id = :courseId")
    long countByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.academicYear = :academicYear AND ar.semester = :semester")
    List<AttendanceRecord> findByAcademicYearAndSemester(@Param("academicYear") String academicYear, @Param("semester") Integer semester);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.academicYear = :academicYear AND ar.semester = :semester")
    List<AttendanceRecord> findByStudentAndAcademicYearAndSemester(@Param("studentId") Long studentId, @Param("academicYear") String academicYear, @Param("semester") Integer semester);
    
    @Query("SELECT AVG(CASE WHEN ar.status = 'PRESENT' THEN 1.0 ELSE 0.0 END) * 100 FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.course.id = :courseId")
    Double calculateAttendancePercentage(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT AVG(CASE WHEN ar.status = 'PRESENT' THEN 1.0 ELSE 0.0 END) * 100 FROM AttendanceRecord ar WHERE ar.course.id = :courseId")
    Double calculateCourseAttendancePercentage(@Param("courseId") Long courseId);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.faculty.id = :facultyId AND ar.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByFacultyIdAndDateRange(@Param("facultyId") Long facultyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Additional missing methods 
    List<AttendanceRecord> findByStudentIdAndCourseIdOrderByAttendanceDateDesc(Long studentId, Long courseId);
    
    List<AttendanceRecord> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    
    List<AttendanceRecord> findByCourseIdOrderByAttendanceDateAsc(Long courseId);
    
    List<AttendanceRecord> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    
    List<AttendanceRecord> findByCourseIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long courseId, LocalDate startDate, LocalDate endDate);
    
    List<AttendanceRecord> findByStudentIdAndCourseIdAndAttendanceDateBetween(Long studentId, Long courseId, LocalDate startDate, LocalDate endDate);
    
    // Additional missing methods - use proper @Query annotations for complex date range queries
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student.id = :studentId AND ar.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByStudentAndDateRange(@Param("studentId") Long studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.course.id = :courseId AND ar.attendanceDate = :date")
    List<AttendanceRecord> findByCourseAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.course.id = :courseId AND ar.status = 'PRESENT'")
    Long getAttendanceSummaryByCourse(@Param("courseId") Long courseId);
    
    List<AttendanceRecord> findByCourseIdAndFacultyId(Long courseId, Long facultyId);
    
    Optional<AttendanceRecord> findByStudentIdAndCourseIdAndAttendanceDate(Long studentId, Long courseId, LocalDate attendanceDate);
}