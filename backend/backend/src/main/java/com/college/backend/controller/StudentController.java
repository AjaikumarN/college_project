package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.model.Student;
import com.college.backend.model.Enrollment;
import com.college.backend.model.Grade;
import com.college.backend.model.AttendanceRecord;
import com.college.backend.service.StudentService;
import com.college.backend.service.EnrollmentService;
import com.college.backend.service.StudentPortalService;
import com.college.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"})
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentPortalService studentPortalService;

    // ============= STUDENT PORTAL FEATURES =============

    // Student Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getStudentDashboard() {
        try {
            Map<String, Object> dashboard = studentPortalService.getStudentDashboardOverview();
            return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", dashboard));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve dashboard: " + e.getMessage()));
        }
    }

    // Course Enrollment Management
    @GetMapping("/portal/courses/available")
    public ResponseEntity<?> getAvailableCoursesForEnrollment() {
        try {
            Map<String, Object> availableCourses = studentPortalService.getAvailableCoursesForEnrollment();
            return ResponseEntity.ok(ApiResponse.success("Available courses retrieved successfully", availableCourses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available courses: " + e.getMessage()));
        }
    }

    @PostMapping("/portal/enroll/{courseId}")
    public ResponseEntity<?> enrollInCoursePortal(@PathVariable Long courseId) {
        try {
            Map<String, Object> enrollmentResult = studentPortalService.enrollInCourse(courseId);
            if ((Boolean) enrollmentResult.get("success")) {
                return ResponseEntity.ok(ApiResponse.success((String) enrollmentResult.get("message"), enrollmentResult));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error((String) enrollmentResult.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Enrollment failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/portal/drop/{courseId}")
    public ResponseEntity<?> dropCoursePortal(@PathVariable Long courseId) {
        try {
            Map<String, Object> dropResult = studentPortalService.dropCourse(courseId);
            if ((Boolean) dropResult.get("success")) {
                return ResponseEntity.ok(ApiResponse.success((String) dropResult.get("message"), dropResult));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error((String) dropResult.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Drop course failed: " + e.getMessage()));
        }
    }

    // Academic Records Portal
    @GetMapping("/portal/academic-records")
    public ResponseEntity<?> getAcademicRecordsPortal() {
        try {
            Map<String, Object> academicRecords = studentPortalService.getAcademicRecords();
            return ResponseEntity.ok(ApiResponse.success("Academic records retrieved successfully", academicRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve academic records: " + e.getMessage()));
        }
    }

    // Attendance Portal
    @GetMapping("/portal/attendance")
    public ResponseEntity<?> getAttendanceRecordsPortal() {
        try {
            Map<String, Object> attendanceRecords = studentPortalService.getAttendanceRecords();
            return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", attendanceRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance records: " + e.getMessage()));
        }
    }

    // Grades Portal
    @GetMapping("/portal/grades")
    public ResponseEntity<?> getGradeDetailsPortal() {
        try {
            Map<String, Object> gradeDetails = studentPortalService.getGradeDetails();
            return ResponseEntity.ok(ApiResponse.success("Grade details retrieved successfully", gradeDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve grade details: " + e.getMessage()));
        }
    }

    // ============= EXISTING STUDENT FEATURES =============

    // Student Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getStudentProfile(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("Student profile retrieved successfully", student));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student profile not found: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateStudentProfile(@Valid @RequestBody Student student, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            student.getUser().setId(userPrincipal.getId());
            
            Student updatedStudent = studentService.updateStudent(student);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedStudent));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    // Academic Records
    @GetMapping("/enrollments")
    public ResponseEntity<?> getMyEnrollments(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<Enrollment> enrollments = studentService.getStudentEnrollments(student.getId());
            return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollments: " + e.getMessage()));
        }
    }

    @GetMapping("/enrollments/year/{academicYear}")
    public ResponseEntity<?> getEnrollmentsByYear(@PathVariable String academicYear, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<Enrollment> enrollments = studentService.getStudentEnrollmentsByAcademicYear(student.getId(), academicYear);
            return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollments: " + e.getMessage()));
        }
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            Enrollment enrollment = enrollmentService.createEnrollment(student.getId(), courseId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Successfully enrolled in course", enrollment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Enrollment failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/enroll/{courseId}")
    public ResponseEntity<?> dropCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            Enrollment enrollment = enrollmentService.dropCourse(student.getId(), courseId);
            return ResponseEntity.ok(ApiResponse.success("Successfully dropped course", enrollment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to drop course: " + e.getMessage()));
        }
    }

    // Grades and Academic Performance
    @GetMapping("/grades")
    public ResponseEntity<?> getMyGrades(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<Grade> grades = studentService.getStudentGrades(student.getId());
            return ResponseEntity.ok(ApiResponse.success("Grades retrieved successfully", grades));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve grades: " + e.getMessage()));
        }
    }

    @GetMapping("/grades/year/{academicYear}")
    public ResponseEntity<?> getGradesByYear(@PathVariable String academicYear, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<Grade> grades = studentService.getStudentGradesByAcademicYear(student.getId(), academicYear);
            return ResponseEntity.ok(ApiResponse.success("Grades retrieved successfully", grades));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve grades: " + e.getMessage()));
        }
    }

    @GetMapping("/gpa")
    public ResponseEntity<?> getMyGPA(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            Double gpa = studentService.getStudentGPA(student.getId());
            return ResponseEntity.ok(ApiResponse.success("GPA retrieved successfully", gpa));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve GPA: " + e.getMessage()));
        }
    }

    // Attendance
    @GetMapping("/attendance")
    public ResponseEntity<?> getMyAttendance(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<AttendanceRecord> attendance = studentService.getStudentAttendance(student.getId());
            return ResponseEntity.ok(ApiResponse.success("Attendance retrieved successfully", attendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance: " + e.getMessage()));
        }
    }

    @GetMapping("/attendance/range")
    public ResponseEntity<?> getAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<AttendanceRecord> attendance = studentService.getStudentAttendanceByDateRange(student.getId(), startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Attendance retrieved successfully", attendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance: " + e.getMessage()));
        }
    }

    @GetMapping("/attendance/percentage/{courseId}")
    public ResponseEntity<?> getAttendancePercentage(@PathVariable Long courseId, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            Double percentage = studentService.getStudentAttendancePercentage(student.getId(), courseId);
            return ResponseEntity.ok(ApiResponse.success("Attendance percentage retrieved successfully", percentage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance percentage: " + e.getMessage()));
        }
    }

    // Credit Information
    @GetMapping("/credits/{academicYear}/{semester}")
    public ResponseEntity<?> getTotalCredits(@PathVariable String academicYear, @PathVariable Integer semester, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            Integer totalCredits = enrollmentService.getStudentTotalCredits(student.getId(), academicYear, semester);
            return ResponseEntity.ok(ApiResponse.success("Total credits retrieved successfully", totalCredits));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve credits: " + e.getMessage()));
        }
    }

    // Academic History
    @GetMapping("/academic-history")
    public ResponseEntity<?> getAcademicHistory(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Student student = studentService.getStudentByUserId(userPrincipal.getId());
            List<Enrollment> completedEnrollments = enrollmentService.getCompletedEnrollmentsByStudent(student.getId());
            return ResponseEntity.ok(ApiResponse.success("Academic history retrieved successfully", completedEnrollments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve academic history: " + e.getMessage()));
        }
    }
}