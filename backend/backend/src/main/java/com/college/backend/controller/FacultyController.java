package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.model.Faculty;
import com.college.backend.model.Course;
import com.college.backend.model.Grade;
import com.college.backend.model.AttendanceRecord;
import com.college.backend.model.Enrollment;
import com.college.backend.service.*;
import com.college.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
@CrossOrigin(origins = "*")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private FacultyDashboardService dashboardService;
    
    @Autowired
    private FacultyCourseManagementService courseManagementService;
    
    @Autowired
    private FacultyStudentManagementService studentManagementService;
    
    @Autowired
    private FacultyGradeManagementService gradeManagementService;
    
    @Autowired
    private FacultyAttendanceManagementService attendanceManagementService;

    // Dashboard Endpoints
    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = dashboardService.getFacultyDashboardOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/dashboard/performance-metrics")
    public ResponseEntity<Map<String, Object>> getTeachingPerformanceMetrics() {
        Map<String, Object> metrics = dashboardService.getTeachingPerformanceMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/dashboard/student-interactions")
    public ResponseEntity<Map<String, Object>> getStudentInteractionAnalytics() {
        Map<String, Object> analytics = dashboardService.getStudentInteractionAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/dashboard/weekly-analytics")
    public ResponseEntity<Map<String, Object>> getWeeklyTeachingAnalytics() {
        Map<String, Object> analytics = dashboardService.getWeeklyTeachingAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Enhanced Course Management Endpoints
    @GetMapping("/courses/management/overview")
    public ResponseEntity<Map<String, Object>> getCourseManagementOverview() {
        Map<String, Object> overview = courseManagementService.getFacultyCourseOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/courses/{courseId}/management/details")
    public ResponseEntity<Map<String, Object>> getCourseManagementDetails(@PathVariable Long courseId) {
        Map<String, Object> details = courseManagementService.getCourseDetails(courseId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/courses/{courseId}/analytics/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedCourseAnalytics(@PathVariable Long courseId) {
        Map<String, Object> analytics = dashboardService.getCourseAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @PutMapping("/courses/{courseId}/content")
    public ResponseEntity<Map<String, Object>> updateCourseContent(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> courseUpdates) {
        courseManagementService.updateCourseContent(courseId, courseUpdates);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Course content updated successfully"
        ));
    }

    @PutMapping("/courses/{courseId}/schedule")
    public ResponseEntity<Map<String, Object>> updateCourseSchedule(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> scheduleData) {
        Map<String, Object> result = courseManagementService.updateCourseSchedule(courseId, scheduleData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}/performance-summary")
    public ResponseEntity<Map<String, Object>> getCoursePerformanceSummary(@PathVariable Long courseId) {
        Map<String, Object> summary = courseManagementService.getCoursePerformanceSummary(courseId);
        return ResponseEntity.ok(summary);
    }

    // Enhanced Student Management Endpoints
    @GetMapping("/students/management/overview")
    public ResponseEntity<Map<String, Object>> getStudentManagementOverview() {
        Map<String, Object> overview = studentManagementService.getStudentManagementOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/courses/{courseId}/students/detailed")
    public ResponseEntity<Map<String, Object>> getStudentsByCourseDetailed(@PathVariable Long courseId) {
        Map<String, Object> students = studentManagementService.getStudentsByCourse(courseId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/courses/{courseId}/students/{studentId}/progress")
    public ResponseEntity<Map<String, Object>> getStudentProgress(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        Map<String, Object> progress = studentManagementService.getStudentProgressInCourse(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/courses/{courseId}/analytics/students")
    public ResponseEntity<Map<String, Object>> getCourseStudentAnalytics(@PathVariable Long courseId) {
        Map<String, Object> analytics = studentManagementService.getCourseStudentAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/courses/{courseId}/students/bulk-operations")
    public ResponseEntity<Map<String, Object>> performBulkStudentOperation(
            @PathVariable Long courseId,
            @RequestBody BulkStudentOperationRequest request) {
        Map<String, Object> result = studentManagementService.performBulkStudentOperation(
            courseId, request.getStudentIds(), request.getOperation(), request.getParameters());
        return ResponseEntity.ok(result);
    }

    // Enhanced Grade Management Endpoints
    @GetMapping("/grades/management/overview")
    public ResponseEntity<Map<String, Object>> getGradeManagementOverview() {
        Map<String, Object> overview = gradeManagementService.getGradeManagementOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/courses/{courseId}/grades/management/overview")
    public ResponseEntity<Map<String, Object>> getCourseGradeManagementOverview(@PathVariable Long courseId) {
        Map<String, Object> overview = gradeManagementService.getCourseGradeOverview(courseId);
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/courses/{courseId}/students/{studentId}/grades/enhanced")
    public ResponseEntity<Map<String, Object>> enterEnhancedGrade(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @RequestBody GradeEntryRequest request) {
        gradeManagementService.enterGrade(courseId, studentId, request.toGrade());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Grade entered successfully"
        ));
    }

    @PostMapping("/courses/{courseId}/grades/bulk-enhanced")
    public ResponseEntity<Map<String, Object>> enterBulkEnhancedGrades(
            @PathVariable Long courseId,
            @RequestBody List<Map<String, Object>> gradeEntries) {
        Map<String, Object> result = gradeManagementService.enterBulkGrades(courseId, gradeEntries);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}/grades/analytics/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedCourseGradeAnalytics(@PathVariable Long courseId) {
        Map<String, Object> analytics = gradeManagementService.getCourseGradeAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/courses/{courseId}/grades/reports")
    public ResponseEntity<Map<String, Object>> generateGradeReport(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "comprehensive") String reportType) {
        Map<String, Object> report = gradeManagementService.generateGradeReport(courseId, reportType);
        return ResponseEntity.ok(report);
    }

    // Enhanced Attendance Management Endpoints
    @GetMapping("/attendance/management/overview")
    public ResponseEntity<Map<String, Object>> getAttendanceManagementOverview() {
        Map<String, Object> overview = attendanceManagementService.getAttendanceManagementOverview();
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/courses/{courseId}/attendance/session-enhanced")
    public ResponseEntity<Map<String, Object>> markSessionAttendanceEnhanced(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestBody List<Map<String, Object>> attendanceData) {
        Map<String, Object> result = attendanceManagementService.markSessionAttendance(
            courseId, attendanceDate, attendanceData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}/attendance/session-detailed")
    public ResponseEntity<Map<String, Object>> getSessionAttendanceDetailed(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate) {
        Map<String, Object> session = attendanceManagementService.getSessionAttendance(courseId, attendanceDate);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/courses/{courseId}/students/{studentId}/attendance/history")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceHistory(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        Map<String, Object> history = attendanceManagementService.getStudentAttendanceHistory(courseId, studentId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/courses/{courseId}/attendance/analytics/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedCourseAttendanceAnalytics(@PathVariable Long courseId) {
        Map<String, Object> analytics = attendanceManagementService.getCourseAttendanceAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/courses/{courseId}/attendance/reports")
    public ResponseEntity<Map<String, Object>> generateAttendanceReport(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "summary") String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = attendanceManagementService.generateAttendanceReport(
            courseId, reportType, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Faculty Profile Management
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFacultyProfile(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Faculty faculty = facultyService.getFacultyByUserId(userPrincipal.getId());
            // Initialize lazy-loaded department to avoid LazyInitializationException
            if (faculty.getDepartment() != null) {
                faculty.getDepartment().getDepartmentName();
            }
            return ResponseEntity.ok(ApiResponse.success("Faculty profile retrieved successfully", faculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Faculty profile not found: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateFacultyProfile(@Valid @RequestBody Faculty faculty, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            faculty.getUser().setId(userPrincipal.getId());
            
            Faculty updatedFaculty = facultyService.updateFaculty(faculty);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedFaculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    // Course Management
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Faculty faculty = facultyService.getFacultyByUserId(userPrincipal.getId());
            List<Course> courses = facultyService.getFacultyCourses(faculty.getId());
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@Valid @RequestBody Course course, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Faculty faculty = facultyService.getFacultyByUserId(userPrincipal.getId());
            course.setInstructor(faculty);
            
            Course createdCourse = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Course created successfully", createdCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create course: " + e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @Valid @RequestBody Course course, Authentication authentication) {
        try {
            Course updatedCourse = courseService.updateCourse(courseId, course);
            return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update course: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<?> getCourseEnrollments(@PathVariable Long courseId) {
        try {
            List<Enrollment> enrollments = facultyService.getCourseEnrollments(courseId);
            return ResponseEntity.ok(ApiResponse.success("Course enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollments: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/enrollments/count")
    public ResponseEntity<?> getCourseEnrollmentCount(@PathVariable Long courseId) {
        try {
            long count = courseService.getCourseEnrollmentCount(courseId);
            return ResponseEntity.ok(ApiResponse.success("Enrollment count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollment count: " + e.getMessage()));
        }
    }

    // Grade Management
    @PostMapping("/grades")
    public ResponseEntity<?> createGrade(@Valid @RequestBody Grade grade) {
        try {
            Grade createdGrade = facultyService.createGrade(grade);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Grade created successfully", createdGrade));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create grade: " + e.getMessage()));
        }
    }

    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId, @Valid @RequestBody Grade grade) {
        try {
            grade.setId(gradeId);
            Grade updatedGrade = facultyService.updateGrade(grade);
            return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", updatedGrade));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update grade: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/grades")
    public ResponseEntity<?> getCourseGrades(@PathVariable Long courseId) {
        try {
            List<Grade> grades = facultyService.getGradesByCourse(courseId);
            return ResponseEntity.ok(ApiResponse.success("Grades retrieved successfully", grades));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve grades: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/grades/{assessmentType}")
    public ResponseEntity<?> getCourseGradesByAssessment(@PathVariable Long courseId, @PathVariable Grade.AssessmentType assessmentType) {
        try {
            List<Grade> grades = facultyService.getGradesByCourseAndAssessment(courseId, assessmentType);
            return ResponseEntity.ok(ApiResponse.success("Grades retrieved successfully", grades));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve grades: " + e.getMessage()));
        }
    }

    // Attendance Management
    @PostMapping("/attendance")
    public ResponseEntity<?> markAttendance(@Valid @RequestBody AttendanceRecord attendance) {
        try {
            AttendanceRecord markedAttendance = facultyService.markAttendance(attendance);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Attendance marked successfully", markedAttendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to mark attendance: " + e.getMessage()));
        }
    }

    @PostMapping("/attendance/bulk")
    public ResponseEntity<?> markBulkAttendance(@Valid @RequestBody List<AttendanceRecord> attendanceRecords) {
        try {
            List<AttendanceRecord> markedAttendance = facultyService.markBulkAttendance(attendanceRecords);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Bulk attendance marked successfully", markedAttendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to mark bulk attendance: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/attendance")
    public ResponseEntity<?> getCourseAttendance(@PathVariable Long courseId) {
        try {
            List<AttendanceRecord> attendance = facultyService.getAttendanceByCourse(courseId);
            return ResponseEntity.ok(ApiResponse.success("Attendance retrieved successfully", attendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/attendance/{date}")
    public ResponseEntity<?> getCourseAttendanceByDate(@PathVariable Long courseId, 
                                                      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AttendanceRecord> attendance = facultyService.getAttendanceByCourseAndDate(courseId, date);
            return ResponseEntity.ok(ApiResponse.success("Attendance retrieved successfully", attendance));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance: " + e.getMessage()));
        }
    }

    @GetMapping("/courses/{courseId}/attendance/summary")
    public ResponseEntity<?> getAttendanceSummary(@PathVariable Long courseId) {
        try {
            Long summary = facultyService.getAttendanceSummaryByCourse(courseId);
            return ResponseEntity.ok(ApiResponse.success("Attendance summary retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve attendance summary: " + e.getMessage()));
        }
    }

    // Student Management
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents(Pageable pageable) {
        try {
            Page<com.college.backend.model.Student> students = facultyService.getAllStudents(pageable);
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve students: " + e.getMessage()));
        }
    }

    @GetMapping("/students/search")
    public ResponseEntity<?> searchStudents(@RequestParam String searchTerm) {
        try {
            List<com.college.backend.model.Student> students = facultyService.searchStudents(searchTerm);
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search students: " + e.getMessage()));
        }
    }

    // Department Operations
    @GetMapping("/department/faculty")
    public ResponseEntity<?> getDepartmentFaculty(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Faculty faculty = facultyService.getFacultyByUserId(userPrincipal.getId());
            
            if (faculty.getDepartment() != null) {
                List<Faculty> departmentFaculty = facultyService.getFacultyByDepartment(faculty.getDepartment().getId());
                return ResponseEntity.ok(ApiResponse.success("Department faculty retrieved successfully", departmentFaculty));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Faculty is not assigned to any department"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve department faculty: " + e.getMessage()));
        }
    }

    // Faculty Statistics
    @GetMapping("/statistics/courses")
    public ResponseEntity<?> getCourseStatistics(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Faculty faculty = facultyService.getFacultyByUserId(userPrincipal.getId());
            List<Course> courses = facultyService.getFacultyCourses(faculty.getId());
            
            long totalCourses = courses.size();
            long activeCourses = courses.stream().mapToLong(course -> 
                course.getStatus() == Course.CourseStatus.ACTIVE ? 1 : 0).sum();
            
            return ResponseEntity.ok(ApiResponse.success("Course statistics retrieved successfully", 
                    java.util.Map.of("totalCourses", totalCourses, "activeCourses", activeCourses)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    // DTOs for Enhanced Request Bodies
    public static class BulkStudentOperationRequest {
        private List<Long> studentIds;
        private String operation;
        private Map<String, Object> parameters;

        // Getters and setters
        public List<Long> getStudentIds() { return studentIds; }
        public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class GradeEntryRequest {
        private String gradeType;
        private String letterGrade;
        private Double numericGrade;
        private Double maxPoints;
        private Double pointsEarned;
        private String comments;

        // Convert to Grade entity
        public Grade toGrade() {
            Grade grade = new Grade();
            grade.setGradeType(this.gradeType);
            grade.setLetterGrade(this.letterGrade);
            grade.setNumericGrade(this.numericGrade);
            grade.setMaxPoints(this.maxPoints);
            grade.setPointsEarned(this.pointsEarned);
            grade.setComments(this.comments);
            return grade;
        }

        // Getters and setters
        public String getGradeType() { return gradeType; }
        public void setGradeType(String gradeType) { this.gradeType = gradeType; }
        public String getLetterGrade() { return letterGrade; }
        public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }
        public Double getNumericGrade() { return numericGrade; }
        public void setNumericGrade(Double numericGrade) { this.numericGrade = numericGrade; }
        public Double getMaxPoints() { return maxPoints; }
        public void setMaxPoints(Double maxPoints) { this.maxPoints = maxPoints; }
        public Double getPointsEarned() { return pointsEarned; }
        public void setPointsEarned(Double pointsEarned) { this.pointsEarned = pointsEarned; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}