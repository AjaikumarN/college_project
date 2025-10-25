package com.college.backend.repository;

import com.college.backend.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    
    Optional<Faculty> findByFacultyId(String facultyId);
    
    Optional<Faculty> findByEmployeeId(String employeeId);
    
    Optional<Faculty> findByUserId(Long userId);
    
    List<Faculty> findByStatus(Faculty.FacultyStatus status);
    
    List<Faculty> findByDepartmentId(Long departmentId);
    
    List<Faculty> findByDesignation(Faculty.Designation designation);
    
    List<Faculty> findByEmploymentType(Faculty.EmploymentType employmentType);
    
    @Query("SELECT f FROM Faculty f WHERE f.isHeadOfDepartment = true")
    List<Faculty> findHeadsOfDepartment();
    
    @Query("SELECT f FROM Faculty f WHERE f.isClassCoordinator = true")
    List<Faculty> findClassCoordinators();
    
    @Query("SELECT f FROM Faculty f WHERE f.experienceYears >= :minExperience ORDER BY f.experienceYears DESC")
    List<Faculty> findExperiencedFaculty(@Param("minExperience") Integer minExperience);
    
    @Query("SELECT f FROM Faculty f WHERE f.qualification LIKE %:qualification%")
    List<Faculty> findByQualification(@Param("qualification") String qualification);
    
    @Query("SELECT f FROM Faculty f WHERE f.specialization LIKE %:specialization%")
    List<Faculty> findBySpecialization(@Param("specialization") String specialization);
    
    @Query("SELECT COUNT(f) FROM Faculty f WHERE f.status = :status")
    long countByStatus(@Param("status") Faculty.FacultyStatus status);
    
    // Added missing methods based on compilation errors
    long countByDesignation(Faculty.Designation designation);
    
    long countByDepartmentId(Long departmentId);
    
    List<Faculty> findByIsDepartmentHead(boolean isDepartmentHead);
    
    List<Faculty> findByExperienceYearsBetween(Integer minYears, Integer maxYears);
    
    List<Faculty> findByExperienceYearsGreaterThanEqual(int years);
    
    List<Faculty> findByExperienceYearsLessThan(int years);
    
    @Query("SELECT f FROM Faculty f WHERE f.user.name LIKE %:searchTerm% OR f.facultyId LIKE %:searchTerm% OR f.user.email LIKE %:searchTerm%")
    List<Faculty> searchFaculty(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT DISTINCT f FROM Faculty f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.department")
    List<Faculty> findAllWithUserAndDepartment();
}