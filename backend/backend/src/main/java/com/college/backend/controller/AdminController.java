package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.model.*;
import com.college.backend.service.AdminService;
import com.college.backend.service.DepartmentService;
import com.college.backend.service.CourseService;
import com.college.backend.service.StudentService;
import com.college.backend.service.FacultyService;
import com.college.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private FacultyService facultyService;

    // Admin Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getAdminProfile(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Admin admin = adminService.getAdminByUserId(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("Admin profile retrieved successfully", admin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Admin profile not found: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(@Valid @RequestBody Admin admin, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            admin.getUser().setId(userPrincipal.getId());
            
            Admin updatedAdmin = adminService.updateAdmin(admin);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedAdmin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        try {
            Page<User> users = adminService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable User.UserRole role) {
        try {
            List<User> users = adminService.getUsersByRole(role);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = adminService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", createdUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody User user) {
        try {
            User updatedUser = adminService.updateUser(userId, user);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            adminService.activateUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to activate user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            adminService.deactivateUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to deactivate user: " + e.getMessage()));
        }
    }

    @GetMapping("/users/quick-search")
    public ResponseEntity<?> searchUsers(@RequestParam String searchTerm) {
        try {
            List<User> users = adminService.searchUsers(searchTerm);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search users: " + e.getMessage()));
        }
    }

    // Student Management
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(Pageable pageable) {
        try {
            System.out.println("=== GET /api/admin/students called ===");
            System.out.println("Pageable: " + pageable);
            Page<Student> students = adminService.getAllStudents(pageable);
            System.out.println("Students found: " + students.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR in getAllStudents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve students: " + e.getMessage()));
        }
    }

    @PutMapping("/students/{studentId}/status")
    public ResponseEntity<?> updateStudentStatus(@PathVariable Long studentId, @RequestBody Student.StudentStatus status) {
        try {
            Student updatedStudent = adminService.updateStudentStatus(studentId, status);
            return ResponseEntity.ok(ApiResponse.success("Student status updated successfully", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update student status: " + e.getMessage()));
        }
    }

    @PutMapping("/students/{studentId}/fee-status")
    public ResponseEntity<?> updateStudentFeeStatus(@PathVariable Long studentId, @RequestBody Student.FeeStatus feeStatus) {
        try {
            Student updatedStudent = adminService.updateStudentFeeStatus(studentId, feeStatus);
            return ResponseEntity.ok(ApiResponse.success("Student fee status updated successfully", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update fee status: " + e.getMessage()));
        }
    }

    @GetMapping("/students/fee-status/{feeStatus}")
    public ResponseEntity<?> getStudentsByFeeStatus(@PathVariable Student.FeeStatus feeStatus) {
        try {
            List<Student> students = adminService.getStudentsByFeeStatus(feeStatus);
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve students: " + e.getMessage()));
        }
    }

    // Faculty Management
    @GetMapping("/faculty")
    public ResponseEntity<?> getAllFaculty(Pageable pageable) {
        try {
            System.out.println("=== GET /api/admin/faculty called ===");
            System.out.println("Pageable: " + pageable);
            Page<Faculty> faculty = adminService.getAllFaculty(pageable);
            System.out.println("Faculty found: " + faculty.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success("Faculty retrieved successfully", faculty));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR in getAllFaculty: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve faculty: " + e.getMessage()));
        }
    }

    @PutMapping("/faculty/{facultyId}/status")
    public ResponseEntity<?> updateFacultyStatus(@PathVariable Long facultyId, @RequestBody Faculty.FacultyStatus status) {
        try {
            Faculty updatedFaculty = adminService.updateFacultyStatus(facultyId, status);
            return ResponseEntity.ok(ApiResponse.success("Faculty status updated successfully", updatedFaculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update faculty status: " + e.getMessage()));
        }
    }

    @PutMapping("/faculty/{facultyId}/designation")
    public ResponseEntity<?> updateFacultyDesignation(@PathVariable Long facultyId, @RequestBody Faculty.Designation designation) {
        try {
            Faculty updatedFaculty = adminService.updateFacultyDesignation(facultyId, designation);
            return ResponseEntity.ok(ApiResponse.success("Faculty designation updated successfully", updatedFaculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update faculty designation: " + e.getMessage()));
        }
    }

    // Department Management
    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<Department> departments = adminService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody Department department) {
        try {
            Department createdDepartment = adminService.createDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Department created successfully", createdDepartment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create department: " + e.getMessage()));
        }
    }

    @PutMapping("/departments/{departmentId}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long departmentId, @Valid @RequestBody Department department) {
        try {
            Department updatedDepartment = adminService.updateDepartment(departmentId, department);
            return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updatedDepartment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update department: " + e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{departmentId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long departmentId) {
        try {
            adminService.deleteDepartment(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to delete department: " + e.getMessage()));
        }
    }

    @PutMapping("/departments/{departmentId}/head/{facultyId}")
    public ResponseEntity<?> assignDepartmentHead(@PathVariable Long departmentId, @PathVariable Long facultyId) {
        try {
            Faculty assignedHead = facultyService.assignAsDepartmentHead(facultyId, departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department head assigned successfully", assignedHead));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to assign department head: " + e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{departmentId}/head")
    public ResponseEntity<?> removeDepartmentHead(@PathVariable Long departmentId) {
        try {
            facultyService.removeDepartmentHead(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department head removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to remove department head: " + e.getMessage()));
        }
    }

    // Course Management
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses(Pageable pageable) {
        try {
            Page<Course> courses = adminService.getAllCourses(pageable);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@Valid @RequestBody Course course) {
        try {
            Course createdCourse = adminService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Course created successfully", createdCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create course: " + e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @Valid @RequestBody Course course) {
        try {
            Course updatedCourse = adminService.updateCourse(courseId, course);
            return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update course: " + e.getMessage()));
        }
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            adminService.deleteCourse(courseId);
            return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to delete course: " + e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}/status")
    public ResponseEntity<?> updateCourseStatus(@PathVariable Long courseId, @RequestBody Course.CourseStatus status) {
        try {
            Course updatedCourse = adminService.updateCourseStatus(courseId, status);
            return ResponseEntity.ok(ApiResponse.success("Course status updated successfully", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update course status: " + e.getMessage()));
        }
    }

    // System Statistics and Analytics
    @GetMapping("/statistics/system")
    public ResponseEntity<?> getSystemStatistics() {
        try {
            Map<String, Object> statistics = adminService.getSystemStatistics();
            return ResponseEntity.ok(ApiResponse.success("System statistics retrieved successfully", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics/user-roles")
    public ResponseEntity<?> getUserRoleDistribution() {
        try {
            Map<String, Long> distribution = adminService.getUserRoleDistribution();
            return ResponseEntity.ok(ApiResponse.success("User role distribution retrieved successfully", distribution));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve role distribution: " + e.getMessage()));
        }
    }

    @GetMapping("/departments/without-head")
    public ResponseEntity<?> getDepartmentsWithoutHead() {
        try {
            List<Department> departments = adminService.getDepartmentsWithoutHead();
            return ResponseEntity.ok(ApiResponse.success("Departments without head retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/available-enrollment")
    public ResponseEntity<?> getCoursesAvailableForEnrollment() {
        try {
            List<Course> courses = adminService.getCoursesAvailableForEnrollment();
            return ResponseEntity.ok(ApiResponse.success("Available courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    // Admin Management
    @GetMapping("/admins/user-managers")
    public ResponseEntity<?> getUserManagers() {
        try {
            List<Admin> admins = adminService.getUserManagers();
            return ResponseEntity.ok(ApiResponse.success("User managers retrieved successfully", admins));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user managers: " + e.getMessage()));
        }
    }

    @GetMapping("/admins/course-managers")
    public ResponseEntity<?> getCourseManagers() {
        try {
            List<Admin> admins = adminService.getCourseManagers();
            return ResponseEntity.ok(ApiResponse.success("Course managers retrieved successfully", admins));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve course managers: " + e.getMessage()));
        }
    }

    @GetMapping("/admins/department-managers")
    public ResponseEntity<?> getDepartmentManagers() {
        try {
            List<Admin> admins = adminService.getDepartmentManagers();
            return ResponseEntity.ok(ApiResponse.success("Department managers retrieved successfully", admins));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve department managers: " + e.getMessage()));
        }
    }

    @GetMapping("/admins/super-admins")
    public ResponseEntity<?> getSuperAdmins() {
        try {
            List<Admin> admins = adminService.getSuperAdmins();
            return ResponseEntity.ok(ApiResponse.success("Super admins retrieved successfully", admins));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve super admins: " + e.getMessage()));
        }
    }
}