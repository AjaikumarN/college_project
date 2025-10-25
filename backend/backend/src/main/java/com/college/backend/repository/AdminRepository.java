package com.college.backend.repository;

import com.college.backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByAdminId(String adminId);
    
    Optional<Admin> findByEmployeeId(String employeeId);
    
    Optional<Admin> findByUserId(Long userId);
    
    List<Admin> findByStatus(Admin.AdminStatus status);
    
    List<Admin> findByAdminType(Admin.AdminType adminType);
    
    List<Admin> findByAccessLevel(Admin.AccessLevel accessLevel);
    
    @Query("SELECT a FROM Admin a WHERE a.canManageUsers = true")
    List<Admin> findUserManagers();
    
    @Query("SELECT a FROM Admin a WHERE a.canManageCourses = true")
    List<Admin> findCourseManagers();
    
    @Query("SELECT a FROM Admin a WHERE a.canManageDepartments = true")
    List<Admin> findDepartmentManagers();
    
    @Query("SELECT a FROM Admin a WHERE a.adminType = 'SUPER_ADMIN'")
    List<Admin> findSuperAdmins();
    
    @Query("SELECT a FROM Admin a WHERE a.departmentAccess LIKE %:departmentCode% OR a.departmentAccess = 'ALL'")
    List<Admin> findByDepartmentAccess(@Param("departmentCode") String departmentCode);
    
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status = :status")
    long countByStatus(@Param("status") Admin.AdminStatus status);
    
    @Query("SELECT a FROM Admin a WHERE a.user.name LIKE %:searchTerm% OR a.adminId LIKE %:searchTerm% OR a.user.email LIKE %:searchTerm%")
    List<Admin> searchAdmins(@Param("searchTerm") String searchTerm);
}