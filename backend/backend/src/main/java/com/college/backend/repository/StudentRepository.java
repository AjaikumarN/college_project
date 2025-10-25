package com.college.backend.repository;

import com.college.backend.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentId(String studentId);
    
    Optional<Student> findByAdmissionNumber(String admissionNumber);
    
    Optional<Student> findByUserId(Long userId);
    
    List<Student> findByStatus(Student.StudentStatus status);
    
    List<Student> findByCurrentYearAndCurrentSemester(Integer year, Integer semester);
    
    List<Student> findByDepartmentId(Long departmentId);
    
    List<Student> findByCourse(String course);
    
    List<Student> findByAcademicYear(String academicYear);
    
    @Query("SELECT s FROM Student s WHERE s.department.id = :departmentId AND s.currentYear = :year")
    List<Student> findByDepartmentAndYear(@Param("departmentId") Long departmentId, @Param("year") Integer year);
    
    @Query("SELECT s FROM Student s WHERE s.cgpa >= :minCgpa ORDER BY s.cgpa DESC")
    List<Student> findTopPerformers(@Param("minCgpa") Double minCgpa);
    
    @Query("SELECT s FROM Student s WHERE s.feeStatus = :feeStatus")
    List<Student> findByFeeStatus(@Param("feeStatus") Student.FeeStatus feeStatus);
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = :status")
    long countByStatus(@Param("status") Student.StudentStatus status);
    
    // Added missing methods based on compilation errors
    long countByFeeStatus(Student.FeeStatus feeStatus);
    
    List<Student> findBySemester(Integer semester);
    
    List<Student> findByHostelResident(boolean hostelResident);
    
    List<Student> findByCgpaGreaterThanEqual(Double cgpa);
    
    List<Student> findByCgpaLessThan(Double cgpa);
    
    long countByDepartmentId(Long departmentId);
    
    @Query("SELECT s FROM Student s WHERE s.user.name LIKE %:searchTerm% OR s.studentId LIKE %:searchTerm% OR s.user.email LIKE %:searchTerm%")
    List<Student> searchStudents(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT s FROM Student s WHERE s.isHostelStudent = true")
    List<Student> findHostelStudents();
    
    // Missing method for Faculty Service
    @Query("SELECT s FROM Student s WHERE s.user.name LIKE %:name% OR s.user.email LIKE %:email%")
    List<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(@Param("name") String name, @Param("email") String email);
    
    @Query("SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.department")
    List<Student> findAllWithUserAndDepartment();
}