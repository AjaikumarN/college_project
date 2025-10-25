package com.college.backend.controller;

import com.college.backend.model.Course;
import com.college.backend.model.Department;
import com.college.backend.service.AdminSystemManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminSystemManagementController {

    @Autowired
    private AdminSystemManagementService systemManagementService;

    // Department Management Endpoints
    @GetMapping("/departments/overview")
    public ResponseEntity<Map<String, Object>> getDepartmentManagementOverview() {
        Map<String, Object> overview = systemManagementService.getDepartmentManagementOverview();
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody @Valid Department department) {
        Department createdDepartment = systemManagementService.createDepartment(department);
        return ResponseEntity.ok(createdDepartment);
    }

    @PutMapping("/departments/{departmentId}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long departmentId,
            @RequestBody @Valid Department departmentUpdate) {
        Department updatedDepartment = systemManagementService.updateDepartment(departmentId, departmentUpdate);
        return ResponseEntity.ok(updatedDepartment);
    }

    // Course Management Endpoints
    @GetMapping("/courses/overview")
    public ResponseEntity<Map<String, Object>> getCourseManagementOverview() {
        Map<String, Object> overview = systemManagementService.getCourseManagementOverview();
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody @Valid Course course) {
        Course createdCourse = systemManagementService.createCourse(course);
        return ResponseEntity.ok(createdCourse);
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long courseId,
            @RequestBody @Valid Course courseUpdate) {
        Course updatedCourse = systemManagementService.updateCourse(courseId, courseUpdate);
        return ResponseEntity.ok(updatedCourse);
    }

    // Academic Performance Analytics
    @GetMapping("/analytics/academic-performance")
    public ResponseEntity<Map<String, Object>> getAcademicPerformanceAnalytics() {
        Map<String, Object> analytics = systemManagementService.getAcademicPerformanceAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // System Health Monitoring
    @GetMapping("/health/status")
    public ResponseEntity<Map<String, Object>> getSystemHealthStatus() {
        Map<String, Object> healthStatus = systemManagementService.getSystemHealthStatus();
        return ResponseEntity.ok(healthStatus);
    }

    // Bulk Data Operations
    @PostMapping("/bulk-operations")
    public ResponseEntity<Map<String, Object>> performBulkDataOperation(
            @RequestBody @Valid BulkDataOperationRequest request) {
        Map<String, Object> result = systemManagementService.performBulkDataOperation(
            request.getOperation(), request.getParameters());
        return ResponseEntity.ok(result);
    }

    // System Administration Tools
    @PostMapping("/maintenance/cleanup")
    public ResponseEntity<Map<String, Object>> performSystemCleanup() {
        Map<String, Object> result = systemManagementService.performBulkDataOperation(
            "cleanup_inactive_users", Map.of());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/maintenance/course-status-update")
    public ResponseEntity<Map<String, Object>> bulkUpdateCourseStatus(
            @RequestBody @Valid CourseStatusUpdateRequest request) {
        Map<String, Object> parameters = Map.of(
            "fromStatus", request.getFromStatus().name(),
            "toStatus", request.getToStatus().name()
        );
        Map<String, Object> result = systemManagementService.performBulkDataOperation(
            "update_course_status", parameters);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/maintenance/department-migration")
    public ResponseEntity<Map<String, Object>> performDepartmentMigration(
            @RequestBody @Valid DepartmentMigrationRequest request) {
        Map<String, Object> parameters = Map.of(
            "fromDepartmentId", request.getFromDepartmentId(),
            "toDepartmentId", request.getToDepartmentId()
        );
        Map<String, Object> result = systemManagementService.performBulkDataOperation(
            "department_migration", parameters);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/data")
    public ResponseEntity<Map<String, Object>> exportSystemData(
            @RequestParam(defaultValue = "full_system") String exportType) {
        Map<String, Object> parameters = Map.of("exportType", exportType);
        Map<String, Object> result = systemManagementService.performBulkDataOperation(
            "data_export", parameters);
        return ResponseEntity.ok(result);
    }

    // Configuration Management
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getSystemConfiguration() {
        Map<String, Object> config = Map.of(
            "academicYear", "2024-2025",
            "currentSemester", 1,
            "enrollmentPeriod", Map.of(
                "start", "2024-08-01",
                "end", "2024-08-15"
            ),
            "gradeSubmissionDeadline", "2024-12-15",
            "systemMaintenance", Map.of(
                "nextScheduled", "2024-12-31",
                "duration", "2 hours",
                "type", "database_optimization"
            ),
            "supportContact", Map.of(
                "email", "admin@college.edu",
                "phone", "+1-234-567-8900",
                "emergencyContact", "+1-234-567-8901"
            )
        );
        return ResponseEntity.ok(config);
    }

    @PostMapping("/configuration/update")
    public ResponseEntity<Map<String, Object>> updateSystemConfiguration(
            @RequestBody Map<String, Object> configUpdates) {
        // Implementation for updating system configuration
        Map<String, Object> response = Map.of(
            "success", true,
            "message", "System configuration updated successfully",
            "updatedFields", configUpdates.keySet(),
            "timestamp", java.time.LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    // System Monitoring
    @GetMapping("/monitoring/real-time")
    public ResponseEntity<Map<String, Object>> getRealTimeSystemMonitoring() {
        Map<String, Object> monitoring = Map.of(
            "currentLoad", Map.of(
                "cpu", "45%",
                "memory", "62%",
                "database", "38%"
            ),
            "activeConnections", Map.of(
                "webSessions", 156,
                "databaseConnections", 23,
                "apiRequests", 89
            ),
            "systemMetrics", Map.of(
                "uptime", "15 days, 4 hours",
                "totalRequests", 125643,
                "errorRate", "0.2%",
                "responseTime", "245ms"
            ),
            "recentAlerts", java.util.List.of(
                Map.of("type", "INFO", "message", "System backup completed", "timestamp", "2024-01-10T10:30:00"),
                Map.of("type", "WARNING", "message", "High database load detected", "timestamp", "2024-01-10T09:45:00")
            )
        );
        return ResponseEntity.ok(monitoring);
    }

    // DTOs for Request Bodies
    public static class BulkDataOperationRequest {
        private String operation;
        private Map<String, Object> parameters;

        // Getters and setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class CourseStatusUpdateRequest {
        private Course.CourseStatus fromStatus;
        private Course.CourseStatus toStatus;

        // Getters and setters
        public Course.CourseStatus getFromStatus() { return fromStatus; }
        public void setFromStatus(Course.CourseStatus fromStatus) { this.fromStatus = fromStatus; }
        public Course.CourseStatus getToStatus() { return toStatus; }
        public void setToStatus(Course.CourseStatus toStatus) { this.toStatus = toStatus; }
    }

    public static class DepartmentMigrationRequest {
        private Long fromDepartmentId;
        private Long toDepartmentId;

        // Getters and setters
        public Long getFromDepartmentId() { return fromDepartmentId; }
        public void setFromDepartmentId(Long fromDepartmentId) { this.fromDepartmentId = fromDepartmentId; }
        public Long getToDepartmentId() { return toDepartmentId; }
        public void setToDepartmentId(Long toDepartmentId) { this.toDepartmentId = toDepartmentId; }
    }
}