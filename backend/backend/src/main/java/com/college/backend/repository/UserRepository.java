package com.college.backend.repository;

import com.college.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    Optional<User> findByVerificationToken(String token);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByIsActiveTrue();
    
    List<User> findByIsVerifiedFalse();
    
    // Added missing methods based on compilation errors
    long countByIsActive(boolean isActive);
    
    long countByIsVerified(boolean isVerified);
    
    List<User> findByRegistrationDateAfter(LocalDateTime date);
    
    List<User> findByLastLoginBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT u FROM User u WHERE u.course = :course AND u.year = :year")
    List<User> findByCourseAndYear(@Param("course") String course, @Param("year") String year);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    // Additional missing method
    List<User> findByIsActiveAndLastLoginBefore(boolean isActive, LocalDateTime date);
}
