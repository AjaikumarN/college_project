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
public class DashboardService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    // System Overview Statistics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // User Statistics
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalUsers", userRepository.count());
        userStats.put("activeUsers", userRepository.countByIsActive(true));
        userStats.put("inactiveUsers", userRepository.countByIsActive(false));
        userStats.put("verifiedUsers", userRepository.countByIsVerified(true));
        userStats.put("unverifiedUsers", userRepository.countByIsVerified(false));
        overview.put("userStatistics", userStats);
        
        // Role Distribution
        Map<String, Long> roleDistribution = new HashMap<>();
        roleDistribution.put("students", userRepository.countByRole(User.UserRole.STUDENT));
        roleDistribution.put("faculty", userRepository.countByRole(User.UserRole.FACULTY));
        roleDistribution.put("admins", userRepository.countByRole(User.UserRole.ADMIN));
        overview.put("roleDistribution", roleDistribution);
        
        // Academic Statistics
        Map<String, Object> academicStats = new HashMap<>();
        academicStats.put("totalDepartments", departmentRepository.count());
        academicStats.put("activeDepartments", departmentRepository.countByStatus(Department.DepartmentStatus.ACTIVE));
        academicStats.put("totalCourses", courseRepository.count());
        academicStats.put("activeCourses", courseRepository.countByStatus(Course.CourseStatus.ACTIVE));
        academicStats.put("totalEnrollments", enrollmentRepository.count());
        academicStats.put("activeEnrollments", enrollmentRepository.findByStatus(Enrollment.EnrollmentStatus.ENROLLED).size());
        overview.put("academicStatistics", academicStats);
        
        return overview;
    }

    // Student Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStudentAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Student Status Distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("active", studentRepository.countByStatus(Student.StudentStatus.ACTIVE));
        statusDistribution.put("inactive", studentRepository.countByStatus(Student.StudentStatus.INACTIVE));
        statusDistribution.put("graduated", studentRepository.countByStatus(Student.StudentStatus.GRADUATED));
        statusDistribution.put("suspended", studentRepository.countByStatus(Student.StudentStatus.SUSPENDED));
        analytics.put("statusDistribution", statusDistribution);
        
        // Fee Status Distribution
        Map<String, Long> feeDistribution = new HashMap<>();
        feeDistribution.put("paid", studentRepository.countByFeeStatus(Student.FeeStatus.PAID));
        feeDistribution.put("pending", studentRepository.countByFeeStatus(Student.FeeStatus.PENDING));
        feeDistribution.put("partial", studentRepository.countByFeeStatus(Student.FeeStatus.PARTIAL));
        feeDistribution.put("overdue", studentRepository.countByFeeStatus(Student.FeeStatus.OVERDUE));
        analytics.put("feeDistribution", feeDistribution);
        
        // Academic Performance Analytics
        List<Student> highPerformers = studentRepository.findByCgpaGreaterThanEqual(8.5);
        List<Student> hostelResidents = studentRepository.findByHostelResident(true);
        
        analytics.put("highPerformers", highPerformers.size());
        analytics.put("hostelResidents", hostelResidents.size());
        analytics.put("totalStudents", studentRepository.count());
        
        return analytics;
    }

    // Faculty Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getFacultyAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Faculty Status Distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("active", facultyRepository.countByStatus(Faculty.FacultyStatus.ACTIVE));
        statusDistribution.put("inactive", facultyRepository.countByStatus(Faculty.FacultyStatus.INACTIVE));
        statusDistribution.put("onLeave", facultyRepository.countByStatus(Faculty.FacultyStatus.ON_LEAVE));
        statusDistribution.put("retired", facultyRepository.countByStatus(Faculty.FacultyStatus.RETIRED));
        analytics.put("statusDistribution", statusDistribution);
        
        // Designation Distribution
        Map<String, Long> designationDistribution = new HashMap<>();
        for (Faculty.Designation designation : Faculty.Designation.values()) {
            long count = facultyRepository.countByDesignation(designation);
            designationDistribution.put(designation.name().toLowerCase(), count);
        }
        analytics.put("designationDistribution", designationDistribution);
        
        // Department-wise Faculty Count
        List<Department> departments = departmentRepository.findAll();
        Map<String, Long> departmentWiseCount = new HashMap<>();
        for (Department dept : departments) {
            long count = facultyRepository.countByDepartmentId(dept.getId());
            departmentWiseCount.put(dept.getDepartmentName(), count);
        }
        analytics.put("departmentWiseCount", departmentWiseCount);
        
        analytics.put("totalFaculty", facultyRepository.count());
        analytics.put("departmentHeads", facultyRepository.findByIsDepartmentHead(true).size());
        
        return analytics;
    }

    // Course and Enrollment Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getCourseAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Course Status Distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("active", courseRepository.countByStatus(Course.CourseStatus.ACTIVE));
        statusDistribution.put("inactive", courseRepository.countByStatus(Course.CourseStatus.INACTIVE));
        statusDistribution.put("archived", courseRepository.countByStatus(Course.CourseStatus.ARCHIVED));
        analytics.put("statusDistribution", statusDistribution);
        
        // Course Type Distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Course.CourseType type : Course.CourseType.values()) {
            List<Course> courses = courseRepository.findByType(type);
            typeDistribution.put(type.name().toLowerCase(), (long) courses.size());
        }
        analytics.put("typeDistribution", typeDistribution);
        
        // Department-wise Course Count
        List<Department> departments = departmentRepository.findAll();
        Map<String, Integer> departmentWiseCourses = new HashMap<>();
        for (Department dept : departments) {
            List<Course> courses = courseRepository.findByDepartmentId(dept.getId());
            departmentWiseCourses.put(dept.getDepartmentName(), courses.size());
        }
        analytics.put("departmentWiseCourses", departmentWiseCourses);
        
        // Enrollment Statistics
        List<Course> availableCourses = courseRepository.findAvailableForEnrollment();
        List<Course> fullCourses = courseRepository.findFullyCourse();
        
        analytics.put("totalCourses", courseRepository.count());
        analytics.put("availableForEnrollment", availableCourses.size());
        analytics.put("fullCourses", fullCourses.size());
        
        return analytics;
    }

    // Recent Activities
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getRecentActivities(int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent User Registrations (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<User> recentUsers = userRepository.findByRegistrationDateAfter(thirtyDaysAgo);
        
        for (int i = 0; i < Math.min(limit, recentUsers.size()); i++) {
            User user = recentUsers.get(i);
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "User Registration");
            activity.put("description", "New user registered: " + user.getEmail());
            activity.put("timestamp", user.getRegistrationDate());
            activities.add(activity);
        }
        
        // Recent Enrollments (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Enrollment> recentEnrollments = enrollmentRepository.findByEnrollmentDateBetween(sevenDaysAgo, LocalDateTime.now());
        
        for (int i = 0; i < Math.min(limit - activities.size(), recentEnrollments.size()); i++) {
            Enrollment enrollment = recentEnrollments.get(i);
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "Course Enrollment");
            activity.put("description", "Student enrolled in course");
            activity.put("timestamp", enrollment.getEnrollmentDate());
            activities.add(activity);
        }
        
        return activities;
    }

    // Department Performance
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getDepartmentPerformance() {
        List<Map<String, Object>> departmentPerformance = new ArrayList<>();
        
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            Map<String, Object> deptData = new HashMap<>();
            deptData.put("departmentName", dept.getDepartmentName());
            deptData.put("departmentCode", dept.getDepartmentCode());
            
            // Faculty Count
            long facultyCount = facultyRepository.countByDepartmentId(dept.getId());
            deptData.put("facultyCount", facultyCount);
            
            // Course Count
            List<Course> courses = courseRepository.findByDepartmentId(dept.getId());
            deptData.put("courseCount", courses.size());
            
            // Student Enrollment Count
            long enrollmentCount = 0;
            for (Course course : courses) {
                enrollmentCount += enrollmentRepository.countByCourseAndStatus(course.getId(), Enrollment.EnrollmentStatus.ENROLLED);
            }
            deptData.put("totalEnrollments", enrollmentCount);
            
            // Average Course Utilization
            double avgUtilization = 0.0;
            if (!courses.isEmpty()) {
                for (Course course : courses) {
                    long enrolled = enrollmentRepository.countByCourseAndStatus(course.getId(), Enrollment.EnrollmentStatus.ENROLLED);
                    avgUtilization += (double) enrolled / course.getEnrollmentLimit() * 100;
                }
                avgUtilization /= courses.size();
            }
            deptData.put("averageUtilization", Math.round(avgUtilization * 100.0) / 100.0);
            
            departmentPerformance.add(deptData);
        }
        
        return departmentPerformance;
    }

    // System Health Monitoring
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSystemHealthMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System Performance Metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemResources = new HashMap<>();
        systemResources.put("totalMemory", runtime.totalMemory());
        systemResources.put("freeMemory", runtime.freeMemory());
        systemResources.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        systemResources.put("maxMemory", runtime.maxMemory());
        metrics.put("systemResources", systemResources);
        
        // Database Status
        try {
            long userCount = userRepository.count();
            metrics.put("databaseStatus", "HEALTHY");
            metrics.put("totalUsers", userCount);
        } catch (Exception e) {
            metrics.put("databaseStatus", "ERROR");
            metrics.put("databaseError", e.getMessage());
        }
        
        return metrics;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Database Connection Status
        try {
            userRepository.count();
            health.put("databaseStatus", "HEALTHY");
        } catch (Exception e) {
            health.put("databaseStatus", "ERROR");
            health.put("databaseError", e.getMessage());
        }
        
        // Data Integrity Checks
        Map<String, Object> integrityChecks = new HashMap<>();
        
        // Check for orphaned records
        List<Student> studentsWithoutUsers = new ArrayList<>();
        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            if (student.getUser() == null) {
                studentsWithoutUsers.add(student);
            }
        }
        integrityChecks.put("orphanedStudents", studentsWithoutUsers.size());
        
        // Check departments without heads
        List<Department> deptsWithoutHead = departmentRepository.findDepartmentsWithoutHead();
        integrityChecks.put("departmentsWithoutHead", deptsWithoutHead.size());
        
        // Check courses without instructors
        List<Course> coursesWithoutInstructor = courseRepository.findAll().stream()
                .filter(course -> course.getInstructor() == null)
                .toList();
        integrityChecks.put("coursesWithoutInstructor", coursesWithoutInstructor.size());
        
        health.put("integrityChecks", integrityChecks);
        
        // System Resources
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemResources = new HashMap<>();
        systemResources.put("totalMemory", runtime.totalMemory());
        systemResources.put("freeMemory", runtime.freeMemory());
        systemResources.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        systemResources.put("maxMemory", runtime.maxMemory());
        health.put("systemResources", systemResources);
        
        return health;
    }

    // Academic Calendar and Alerts
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAcademicAlerts() {
        List<Map<String, Object>> alertsList = new ArrayList<>();
        
        // Critical Alerts
        long pendingFees = studentRepository.countByFeeStatus(Student.FeeStatus.OVERDUE);
        if (pendingFees > 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "CRITICAL");
            alert.put("message", pendingFees + " students have overdue fees");
            alert.put("timestamp", LocalDateTime.now());
            alertsList.add(alert);
        }
        
        List<Department> deptsWithoutHead = departmentRepository.findDepartmentsWithoutHead();
        if (!deptsWithoutHead.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "CRITICAL");
            alert.put("message", deptsWithoutHead.size() + " departments without department heads");
            alert.put("timestamp", LocalDateTime.now());
            alertsList.add(alert);
        }
        
        // Warning Alerts
        long inactiveUsers = userRepository.countByIsActive(false);
        if (inactiveUsers > 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "WARNING");
            alert.put("message", inactiveUsers + " inactive user accounts");
            alert.put("timestamp", LocalDateTime.now());
            alertsList.add(alert);
        }
        
        List<Enrollment> pendingGrades = enrollmentRepository.findPendingGrades();
        if (pendingGrades.size() > 50) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "WARNING");
            alert.put("message", pendingGrades.size() + " pending grade entries");
            alert.put("timestamp", LocalDateTime.now());
            alertsList.add(alert);
        }
        
        // Info Alerts
        long recentRegistrations = userRepository.findByRegistrationDateAfter(LocalDateTime.now().minusDays(7)).size();
        if (recentRegistrations > 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "INFO");
            alert.put("message", recentRegistrations + " new user registrations this week");
            alert.put("timestamp", LocalDateTime.now());
            alertsList.add(alert);
        }
        
        return alertsList;
    }
}