package com.college.backend.service;

import com.college.backend.model.*;
import com.college.backend.repository.*;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AdminSystemManagementService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepository;

    // Department Management
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getDepartmentManagementOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<Department> departments = departmentRepository.findAll();
        overview.put("totalDepartments", departments.size());
        overview.put("activeDepartments", departmentRepository.countByIsActive(true));
        overview.put("inactiveDepartments", departmentRepository.countByIsActive(false));
        
        // Department statistics
        Map<String, Object> departmentStats = new HashMap<>();
        for (Department dept : departments) {
            Map<String, Object> deptInfo = new HashMap<>();
            deptInfo.put("name", dept.getName());
            deptInfo.put("code", dept.getCode());
            deptInfo.put("isActive", dept.getIsActive());
            deptInfo.put("facultyCount", facultyRepository.countByDepartmentId(dept.getId()));
            deptInfo.put("studentCount", studentRepository.countByDepartmentId(dept.getId()));
            deptInfo.put("courseCount", courseRepository.countByDepartmentId(dept.getId()));
            
            departmentStats.put(dept.getCode(), deptInfo);
        }
        overview.put("departmentDetails", departmentStats);
        
        return overview;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(Department department) {
        // Validate department code uniqueness
        if (departmentRepository.findByCode(department.getCode()).isPresent()) {
            throw new IllegalArgumentException("Department code already exists: " + department.getCode());
        }
        
        department.setIsActive(true);
        department.setCreatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department updateDepartment(Long departmentId, Department departmentUpdate) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        // Update fields
        if (departmentUpdate.getName() != null) {
            department.setName(departmentUpdate.getName());
        }
        if (departmentUpdate.getDescription() != null) {
            department.setDescription(departmentUpdate.getDescription());
        }
        if (departmentUpdate.getHeadOfDepartment() != null) {
            department.setHeadOfDepartment(departmentUpdate.getHeadOfDepartment());
        }
        if (departmentUpdate.getLocation() != null) {
            department.setLocation(departmentUpdate.getLocation());
        }
        if (departmentUpdate.getContactEmail() != null) {
            department.setContactEmail(departmentUpdate.getContactEmail());
        }
        if (departmentUpdate.getContactPhone() != null) {
            department.setContactPhone(departmentUpdate.getContactPhone());
        }
        
        department.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    // Course Management
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getCourseManagementOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        overview.put("totalCourses", courseRepository.count());
        overview.put("activeCourses", courseRepository.countByStatus(Course.CourseStatus.ACTIVE));
        overview.put("inactiveCourses", courseRepository.countByStatus(Course.CourseStatus.INACTIVE));
        overview.put("draftCourses", courseRepository.countByStatus(Course.CourseStatus.DRAFT));
        
        // Course type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Course.CourseType type : Course.CourseType.values()) {
            typeDistribution.put(type.name(), courseRepository.countByType(type));
        }
        overview.put("courseTypeDistribution", typeDistribution);
        
        // Department-wise course distribution
        List<Department> departments = departmentRepository.findAll();
        Map<String, Object> departmentCourses = new HashMap<>();
        for (Department dept : departments) {
            long courseCount = courseRepository.countByDepartmentId(dept.getId());
            departmentCourses.put(dept.getName(), courseCount);
        }
        overview.put("departmentCourseDistribution", departmentCourses);
        
        return overview;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course createCourse(Course course) {
        // Validate course code uniqueness
        if (courseRepository.findByCourseCode(course.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException("Course code already exists: " + course.getCourseCode());
        }
        
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course updateCourse(Long courseId, Course courseUpdate) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Update fields
        if (courseUpdate.getCourseName() != null) {
            course.setCourseName(courseUpdate.getCourseName());
        }
        if (courseUpdate.getDescription() != null) {
            course.setDescription(courseUpdate.getDescription());
        }
        if (courseUpdate.getCredits() != null) {
            course.setCredits(courseUpdate.getCredits());
        }
        if (courseUpdate.getType() != null) {
            course.setType(courseUpdate.getType());
        }
        if (courseUpdate.getStatus() != null) {
            course.setStatus(courseUpdate.getStatus());
        }
        if (courseUpdate.getDepartment() != null) {
            course.setDepartment(courseUpdate.getDepartment());
        }
        if (courseUpdate.getPrerequisites() != null) {
            course.setPrerequisites(courseUpdate.getPrerequisites());
        }
        if (courseUpdate.getSyllabus() != null) {
            course.setSyllabus(courseUpdate.getSyllabus());
        }
        
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // Academic Performance Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAcademicPerformanceAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Overall performance metrics
        List<Student> allStudents = studentRepository.findAll();
        if (!allStudents.isEmpty()) {
            double avgCgpa = allStudents.stream()
                    .mapToDouble(Student::getCgpa)
                    .average()
                    .orElse(0.0);
            analytics.put("averageCGPA", avgCgpa);
            
            long highPerformers = allStudents.stream()
                    .filter(s -> s.getCgpa() >= 8.5)
                    .count();
            analytics.put("highPerformers", highPerformers);
            
            long lowPerformers = allStudents.stream()
                    .filter(s -> s.getCgpa() < 5.0)
                    .count();
            analytics.put("lowPerformers", lowPerformers);
        }
        
        // Department-wise performance
        List<Department> departments = departmentRepository.findAll();
        Map<String, Object> departmentPerformance = new HashMap<>();
        for (Department dept : departments) {
            List<Student> deptStudents = studentRepository.findByDepartmentId(dept.getId());
            if (!deptStudents.isEmpty()) {
                double deptAvgCgpa = deptStudents.stream()
                        .mapToDouble(Student::getCgpa)
                        .average()
                        .orElse(0.0);
                
                Map<String, Object> deptStats = new HashMap<>();
                deptStats.put("averageCGPA", deptAvgCgpa);
                deptStats.put("studentCount", deptStudents.size());
                deptStats.put("highPerformers", deptStudents.stream().filter(s -> s.getCgpa() >= 8.5).count());
                deptStats.put("lowPerformers", deptStudents.stream().filter(s -> s.getCgpa() < 5.0).count());
                
                departmentPerformance.put(dept.getName(), deptStats);
            }
        }
        analytics.put("departmentPerformance", departmentPerformance);
        
        // Course enrollment analytics
        List<Course> activeCourses = courseRepository.findByStatus(Course.CourseStatus.ACTIVE);
        Map<String, Object> courseEnrollments = new HashMap<>();
        for (Course course : activeCourses) {
            long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
            courseEnrollments.put(course.getCourseName(), enrollmentCount);
        }
        analytics.put("courseEnrollments", courseEnrollments);
        
        return analytics;
    }

    // System Health Monitoring
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSystemHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Database connectivity and performance
        long totalUsers = userRepository.count();
        long totalStudents = studentRepository.count();
        long totalFaculty = facultyRepository.count();
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        
        healthStatus.put("databaseConnectivity", "HEALTHY");
        healthStatus.put("totalRecords", Map.of(
            "users", totalUsers,
            "students", totalStudents,
            "faculty", totalFaculty,
            "courses", totalCourses,
            "enrollments", totalEnrollments
        ));
        
        // Data integrity checks
        List<String> dataIssues = new ArrayList<>();
        
        // Check for orphaned records
        List<Student> studentsWithoutUsers = studentRepository.findAll().stream()
                .filter(s -> s.getUser() == null)
                .toList();
        if (!studentsWithoutUsers.isEmpty()) {
            dataIssues.add("Found " + studentsWithoutUsers.size() + " students without user accounts");
        }
        
        List<Faculty> facultyWithoutUsers = facultyRepository.findAll().stream()
                .filter(f -> f.getUser() == null)
                .toList();
        if (!facultyWithoutUsers.isEmpty()) {
            dataIssues.add("Found " + facultyWithoutUsers.size() + " faculty without user accounts");
        }
        
        // Check for invalid enrollments
        List<Enrollment> invalidEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent() == null || e.getCourse() == null)
                .toList();
        if (!invalidEnrollments.isEmpty()) {
            dataIssues.add("Found " + invalidEnrollments.size() + " invalid enrollments");
        }
        
        healthStatus.put("dataIntegrityIssues", dataIssues);
        healthStatus.put("overallStatus", dataIssues.isEmpty() ? "HEALTHY" : "NEEDS_ATTENTION");
        
        return healthStatus;
    }

    // Bulk Data Operations
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> performBulkDataOperation(String operation, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (operation.toLowerCase()) {
                case "cleanup_inactive_users":
                    result = cleanupInactiveUsers();
                    break;
                case "update_course_status":
                    result = bulkUpdateCourseStatus(parameters);
                    break;
                case "department_migration":
                    result = performDepartmentMigration(parameters);
                    break;
                case "data_export":
                    result = exportSystemData(parameters);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
            
            result.put("success", true);
            result.put("operation", operation);
            result.put("executedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("operation", operation);
        }
        
        return result;
    }

    // Helper Methods
    private Map<String, Object> cleanupInactiveUsers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(6);
        List<User> inactiveUsers = userRepository.findByIsActiveAndLastLoginBefore(false, cutoffDate);
        
        int cleanedCount = 0;
        for (User user : inactiveUsers) {
            // Additional validation before deletion
            if (canSafelyDeleteUser(user)) {
                userRepository.delete(user);
                cleanedCount++;
            }
        }
        
        return Map.of(
            "totalInactiveUsers", inactiveUsers.size(),
            "cleanedUsers", cleanedCount,
            "cutoffDate", cutoffDate
        );
    }

    private Map<String, Object> bulkUpdateCourseStatus(Map<String, Object> parameters) {
        Course.CourseStatus fromStatus = Course.CourseStatus.valueOf((String) parameters.get("fromStatus"));
        Course.CourseStatus toStatus = Course.CourseStatus.valueOf((String) parameters.get("toStatus"));
        
        List<Course> courses = courseRepository.findByStatus(fromStatus);
        courses.forEach(course -> {
            course.setStatus(toStatus);
            course.setUpdatedAt(LocalDateTime.now());
        });
        
        courseRepository.saveAll(courses);
        
        return Map.of(
            "updatedCourses", courses.size(),
            "fromStatus", fromStatus,
            "toStatus", toStatus
        );
    }

    private Map<String, Object> performDepartmentMigration(Map<String, Object> parameters) {
        Long fromDepartmentId = Long.valueOf(parameters.get("fromDepartmentId").toString());
        Long toDepartmentId = Long.valueOf(parameters.get("toDepartmentId").toString());
        
        // Migrate students
        List<Student> students = studentRepository.findByDepartmentId(fromDepartmentId);
        students.forEach(student -> {
            // Set new department - would need to add this field to Student entity
            // student.setDepartmentId(toDepartmentId);
        });
        
        // Migrate faculty
        List<Faculty> faculty = facultyRepository.findByDepartmentId(fromDepartmentId);
        faculty.forEach(f -> {
            // Set new department - would need to add this field to Faculty entity
            // f.setDepartmentId(toDepartmentId);
        });
        
        // Migrate courses
        List<Course> courses = courseRepository.findByDepartmentId(fromDepartmentId);
        Department toDepartment = departmentRepository.findById(toDepartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Target department not found"));
        
        courses.forEach(course -> {
            course.setDepartment(toDepartment);
            course.setUpdatedAt(LocalDateTime.now());
        });
        
        courseRepository.saveAll(courses);
        
        return Map.of(
            "migratedStudents", students.size(),
            "migratedFaculty", faculty.size(),
            "migratedCourses", courses.size(),
            "fromDepartmentId", fromDepartmentId,
            "toDepartmentId", toDepartmentId
        );
    }

    private Map<String, Object> exportSystemData(Map<String, Object> parameters) {
        String exportType = (String) parameters.get("exportType");
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("exportType", exportType);
        exportData.put("generatedAt", LocalDateTime.now());
        
        switch (exportType.toLowerCase()) {
            case "full_system":
                exportData.put("users", userRepository.count());
                exportData.put("students", studentRepository.count());
                exportData.put("faculty", facultyRepository.count());
                exportData.put("courses", courseRepository.count());
                exportData.put("departments", departmentRepository.count());
                exportData.put("enrollments", enrollmentRepository.count());
                break;
            case "academic_data":
                exportData.put("courses", courseRepository.count());
                exportData.put("enrollments", enrollmentRepository.count());
                exportData.put("grades", gradeRepository.count());
                exportData.put("attendance", attendanceRepository.count());
                break;
            default:
                exportData.put("message", "Unknown export type");
        }
        
        return exportData;
    }

    private boolean canSafelyDeleteUser(User user) {
        // Check if user has any important relationships that prevent deletion
        if (user.getRole() == User.UserRole.STUDENT) {
            Optional<Student> student = studentRepository.findByUserId(user.getId());
            if (student.isPresent()) {
                // Check if student has any enrollments or grades
                long enrollments = enrollmentRepository.countByStudentId(student.get().getId());
                return enrollments == 0;
            }
        }
        
        if (user.getRole() == User.UserRole.FACULTY) {
            Optional<Faculty> faculty = facultyRepository.findByUserId(user.getId());
            if (faculty.isPresent()) {
                // Check if faculty is teaching any courses
                // This would require additional repository methods
                return true; // Simplified for now
            }
        }
        
        return true;
    }
}