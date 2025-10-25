package com.college.backend.repository;

import com.college.backend.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByDepartmentCode(String departmentCode);
    
    Optional<Department> findByDepartmentName(String departmentName);
    
    // Alternative methods for field name compatibility
    @Query("SELECT d FROM Department d WHERE d.departmentCode = :code")
    Optional<Department> findByCode(@Param("code") String code);
    
    @Query("SELECT d FROM Department d WHERE d.departmentName = :name")
    Optional<Department> findByName(@Param("name") String name);
    
    List<Department> findByStatus(Department.DepartmentStatus status);
    
    // Added missing methods
    long countByStatus(Department.DepartmentStatus status);
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.status = 'ACTIVE'")
    long countByIsActive(boolean isActive);
    
    List<Department> findByEstablishedYear(Integer establishedYear);
    
    @Query("SELECT d FROM Department d WHERE d.headOfDepartment.id = :facultyId")
    Optional<Department> findByHeadOfDepartmentId(@Param("facultyId") Long facultyId);
    
    @Query("SELECT d FROM Department d WHERE d.headOfDepartment IS NULL")
    List<Department> findDepartmentsWithoutHead();
    
    @Query("SELECT d FROM Department d WHERE size(d.faculty) >= :minSize")
    List<Department> findByMinimumFacultySize(@Param("minSize") int minSize);
    
    @Query("SELECT d FROM Department d WHERE size(d.courses) >= :minCourses")
    List<Department> findByMinimumCourseCount(@Param("minCourses") int minCourses);
    
    @Query("SELECT d FROM Department d WHERE d.departmentName LIKE %:searchTerm% OR d.departmentCode LIKE %:searchTerm%")
    List<Department> searchDepartments(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT d FROM Department d ORDER BY size(d.faculty) DESC")
    List<Department> findByFacultyCountDesc();
    
    @Query("SELECT d FROM Department d ORDER BY size(d.courses) DESC")
    List<Department> findByCourseCountDesc();
    
    @Query("SELECT d FROM Department d WHERE d.establishedYear BETWEEN :startYear AND :endYear")
    List<Department> findByEstablishedYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
}