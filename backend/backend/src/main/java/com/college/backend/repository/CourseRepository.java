package com.college.backend.repository;

import com.college.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByCourseName(String courseName);
    
    List<Course> findByDepartmentId(Long departmentId);
    
    List<Course> findByInstructorId(Long instructorId);
    
    List<Course> findByCredits(Integer credits);
    
    List<Course> findBySemester(Integer semester);
    
    List<Course> findByAcademicYear(String academicYear);
    
    List<Course> findByStatus(Course.CourseStatus status);
    
    List<Course> findByType(Course.CourseType type);
    
    @Query("SELECT c FROM Course c WHERE c.enrollmentLimit > (SELECT COUNT(e) FROM Enrollment e WHERE e.course = c)")
    List<Course> findAvailableForEnrollment();
    
    @Query("SELECT c FROM Course c WHERE c.enrollmentLimit <= (SELECT COUNT(e) FROM Enrollment e WHERE e.course = c)")
    List<Course> findFullyCourse();
    
    @Query("SELECT c FROM Course c WHERE c.department.departmentCode = :departmentCode")
    List<Course> findByDepartmentCode(@Param("departmentCode") String departmentCode);
    
    @Query("SELECT c FROM Course c WHERE c.courseName LIKE %:searchTerm% OR c.courseCode LIKE %:searchTerm% OR c.description LIKE %:searchTerm%")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Course c WHERE c.credits BETWEEN :minCredits AND :maxCredits")
    List<Course> findByCreditRange(@Param("minCredits") Integer minCredits, @Param("maxCredits") Integer maxCredits);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long getEnrollmentCount(@Param("courseId") Long courseId);
    
    @Query("SELECT c FROM Course c WHERE c.prerequisites IS NOT NULL AND c.prerequisites != ''")
    List<Course> findCoursesWithPrerequisites();
    
    @Query("SELECT c FROM Course c WHERE c.prerequisites IS NULL OR c.prerequisites = ''")
    List<Course> findCoursesWithoutPrerequisites();
    
    @Query("SELECT c FROM Course c WHERE c.prerequisites LIKE %:prerequisite%")
    List<Course> findByPrerequisite(@Param("prerequisite") String prerequisite);
    
    @Query("SELECT c FROM Course c WHERE c.semester = :semester AND c.academicYear = :academicYear")
    List<Course> findBySemesterAndAcademicYear(@Param("semester") Integer semester, @Param("academicYear") String academicYear);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status")
    long countByStatus(@Param("status") Course.CourseStatus status);
    
    // Additional missing methods
    long countByDepartmentId(Long departmentId);
    
    long countByType(Course.CourseType type);
}
