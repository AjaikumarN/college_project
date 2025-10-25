package com.college.backend.service;

import com.college.backend.model.Admin;
import com.college.backend.model.User;
import com.college.backend.model.Student;
import com.college.backend.model.Faculty;
import com.college.backend.model.Department;
import com.college.backend.model.Course;
import com.college.backend.repository.AdminRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.repository.StudentRepository;
import com.college.backend.repository.FacultyRepository;
import com.college.backend.repository.DepartmentRepository;
import com.college.backend.repository.CourseRepository;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Admin Profile Management
    @PreAuthorize("hasRole('ADMIN')")
    public Admin getAdminByUserId(Long userId) {
        return adminRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found for user ID: " + userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Admin getAdminByEmployeeId(String employeeId) {
        return adminRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with employee ID: " + employeeId));
    }

    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SUPER_ADMIN')")
    public Admin createAdmin(Admin admin) {
        validateAdmin(admin);
        
        // Validate user exists
        User user = userRepository.findById(admin.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Set user role to ADMIN
        user.setRole(User.UserRole.ADMIN);
        userRepository.save(user);
        
        admin.setUser(user);
        return adminRepository.save(admin);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Admin updateAdmin(Admin admin) {
        Admin existingAdmin = getAdminById(admin.getId());
        
        // Update allowed fields
        existingAdmin.setPhone(admin.getPhone());
        existingAdmin.setAddress(admin.getAddress());
        existingAdmin.setEmergencyContact(admin.getEmergencyContact());
        
        // Super admin only updates
        if (isSuperAdmin()) {
            existingAdmin.setStatus(admin.getStatus());
            existingAdmin.setAdminType(admin.getAdminType());
            existingAdmin.setAccessLevel(admin.getAccessLevel());
            existingAdmin.setCanManageUsers(admin.getCanManageUsers());
            existingAdmin.setCanManageCourses(admin.getCanManageCourses());
            existingAdmin.setCanManageDepartments(admin.getCanManageDepartments());
            existingAdmin.setDepartmentAccess(admin.getDepartmentAccess());
        }
        
        return adminRepository.save(existingAdmin);
    }

    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SUPER_ADMIN')")
    public void deleteAdmin(Long adminId) {
        Admin admin = getAdminById(adminId);
        adminRepository.delete(admin);
    }

    // User Management
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setIsActive(true);
        user.setIsVerified(true);
        
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(Long userId, User userUpdates) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update allowed fields
        existingUser.setName(userUpdates.getName());
        existingUser.setPhone(userUpdates.getPhone());
        existingUser.setIsActive(userUpdates.getIsActive());
        existingUser.setRole(userUpdates.getRole());
        
        return userRepository.save(existingUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }

    // Student Management
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Student> getAllStudents(Pageable pageable) {
        List<Student> allStudents = studentRepository.findAllWithUserAndDepartment();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allStudents.size());
        return new PageImpl<>(allStudents.subList(start, end), pageable, allStudents.size());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Student updateStudentStatus(Long studentId, Student.StudentStatus status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setStatus(status);
        return studentRepository.save(student);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Student updateStudentFeeStatus(Long studentId, Student.FeeStatus feeStatus) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setFeeStatus(feeStatus);
        return studentRepository.save(student);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Student> getStudentsByFeeStatus(Student.FeeStatus feeStatus) {
        return studentRepository.findByFeeStatus(feeStatus);
    }

    // Faculty Management
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Faculty> getAllFaculty(Pageable pageable) {
        List<Faculty> allFaculty = facultyRepository.findAllWithUserAndDepartment();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFaculty.size());
        return new PageImpl<>(allFaculty.subList(start, end), pageable, allFaculty.size());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Faculty updateFacultyStatus(Long facultyId, Faculty.FacultyStatus status) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        faculty.setStatus(status);
        return facultyRepository.save(faculty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Faculty updateFacultyDesignation(Long facultyId, Faculty.Designation designation) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        faculty.setDesignation(designation);
        return facultyRepository.save(faculty);
    }

    // Department Management
    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(Department department) {
        // Check for duplicate department code
        if (departmentRepository.findByDepartmentCode(department.getDepartmentCode()).isPresent()) {
            throw new IllegalArgumentException("Department code already exists");
        }
        
        return departmentRepository.save(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department updateDepartment(Long departmentId, Department departmentUpdates) {
        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        existingDepartment.setDepartmentName(departmentUpdates.getDepartmentName());
        existingDepartment.setDescription(departmentUpdates.getDescription());
        existingDepartment.setStatus(departmentUpdates.getStatus());
        existingDepartment.setHeadOfDepartment(departmentUpdates.getHeadOfDepartment());
        
        return departmentRepository.save(existingDepartment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        departmentRepository.delete(department);
    }

    // Course Management
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course createCourse(Course course) {
        // Check for duplicate course code
        if (courseRepository.findByCourseCode(course.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException("Course code already exists");
        }
        
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course updateCourse(Long courseId, Course courseUpdates) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        existingCourse.setCourseName(courseUpdates.getCourseName());
        existingCourse.setDescription(courseUpdates.getDescription());
        existingCourse.setCredits(courseUpdates.getCredits());
        existingCourse.setStatus(courseUpdates.getStatus());
        existingCourse.setEnrollmentLimit(courseUpdates.getEnrollmentLimit());
        existingCourse.setInstructor(courseUpdates.getInstructor());
        existingCourse.setDepartment(courseUpdates.getDepartment());
        
        return courseRepository.save(existingCourse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        courseRepository.delete(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course updateCourseStatus(Long courseId, Course.CourseStatus status) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setStatus(status);
        return courseRepository.save(course);
    }

    // System Analytics and Reports
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalFaculty", facultyRepository.count());
        stats.put("totalDepartments", departmentRepository.count());
        stats.put("totalCourses", courseRepository.count());
        
        stats.put("activeStudents", studentRepository.countByStatus(Student.StudentStatus.ACTIVE));
        stats.put("activeFaculty", facultyRepository.countByStatus(Faculty.FacultyStatus.ACTIVE));
        stats.put("activeCourses", courseRepository.countByStatus(Course.CourseStatus.ACTIVE));
        
        stats.put("pendingFees", studentRepository.countByFeeStatus(Student.FeeStatus.PENDING));
        stats.put("partialFees", studentRepository.countByFeeStatus(Student.FeeStatus.PARTIAL));
        
        return stats;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getUserRoleDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        distribution.put("students", userRepository.countByRole(User.UserRole.STUDENT));
        distribution.put("faculty", userRepository.countByRole(User.UserRole.FACULTY));
        distribution.put("admins", userRepository.countByRole(User.UserRole.ADMIN));
        
        return distribution;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsWithoutHead() {
        return departmentRepository.findDepartmentsWithoutHead();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Course> getCoursesAvailableForEnrollment() {
        return courseRepository.findAvailableForEnrollment();
    }

    // Admin Specific Queries
    @PreAuthorize("hasRole('ADMIN')")
    public List<Admin> getUserManagers() {
        return adminRepository.findUserManagers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Admin> getCourseManagers() {
        return adminRepository.findCourseManagers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Admin> getDepartmentManagers() {
        return adminRepository.findDepartmentManagers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Admin> getSuperAdmins() {
        return adminRepository.findSuperAdmins();
    }

    // Validation Methods
    private void validateAdmin(Admin admin) {
        if (admin.getUser() == null) {
            throw new IllegalArgumentException("Admin must be associated with a user");
        }
        
        if (admin.getAdminId() == null || admin.getAdminId().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID is required");
        }
        
        // Check for duplicate admin ID
        Optional<Admin> existingAdmin = adminRepository.findByAdminId(admin.getAdminId());
        if (existingAdmin.isPresent() && !existingAdmin.get().getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Admin ID already exists");
        }
    }

    private boolean isSuperAdmin() {
        // Implementation would check current user's admin type
        // This is a placeholder - actual implementation would use SecurityContextHolder
        return true; // Simplified for now
    }
}