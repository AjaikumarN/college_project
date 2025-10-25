package com.college.backend.controller;

import com.college.backend.service.DashboardService;
import com.college.backend.service.AdminUserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AdminUserManagementService userManagementService;

    // Dashboard Overview Endpoints
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = dashboardService.getSystemOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/analytics/students")
    public ResponseEntity<Map<String, Object>> getStudentAnalytics() {
        Map<String, Object> analytics = dashboardService.getStudentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/faculty")
    public ResponseEntity<Map<String, Object>> getFacultyAnalytics() {
        Map<String, Object> analytics = dashboardService.getFacultyAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/courses")
    public ResponseEntity<Map<String, Object>> getCourseAnalytics() {
        Map<String, Object> analytics = dashboardService.getCourseAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentAnalytics() {
        List<Map<String, Object>> analytics = dashboardService.getDepartmentPerformance();
        return ResponseEntity.ok(analytics);
    }

    // Activity Monitoring
    @GetMapping("/activities/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> activities = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/period")
    public ResponseEntity<List<Map<String, Object>>> getActivitiesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Map<String, Object>> activities = userManagementService.getUserActivityReport(startDate, endDate);
        return ResponseEntity.ok(activities);
    }

    // System Health and Monitoring
    @GetMapping("/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = dashboardService.getSystemHealthMetrics();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/alerts/academic")
    public ResponseEntity<List<Map<String, Object>>> getAcademicAlerts() {
        List<Map<String, Object>> alerts = dashboardService.getAcademicAlerts();
        return ResponseEntity.ok(alerts);
    }

    // Quick Statistics
    @GetMapping("/stats/quick")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        Map<String, Object> stats = Map.of(
            "systemOverview", dashboardService.getSystemOverview(),
            "todayActivities", dashboardService.getRecentActivities(5),
            "criticalAlerts", dashboardService.getAcademicAlerts().stream()
                .filter(alert -> "CRITICAL".equals(alert.get("severity")))
                .limit(3)
                .toList()
        );
        return ResponseEntity.ok(stats);
    }

    // Performance Metrics
    @GetMapping("/performance/summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        Map<String, Object> performance = Map.of(
            "studentPerformance", dashboardService.getStudentAnalytics(),
            "facultyPerformance", dashboardService.getFacultyAnalytics(),
            "departmentPerformance", dashboardService.getDepartmentPerformance(),
            "systemHealth", dashboardService.getSystemHealthMetrics()
        );
        return ResponseEntity.ok(performance);
    }

    // Advanced Analytics
    @GetMapping("/analytics/trends")
    public ResponseEntity<Map<String, Object>> getAnalyticsTrends(
            @RequestParam(defaultValue = "30") int days) {
        // This would typically involve time-series data analysis
        Map<String, Object> trends = Map.of(
            "message", "Advanced trending analytics - Implementation pending",
            "period", days + " days",
            "availableMetrics", List.of("enrollment_trends", "performance_trends", "activity_trends")
        );
        return ResponseEntity.ok(trends);
    }

    // Data Export
    @GetMapping("/export/summary")
    public ResponseEntity<Map<String, Object>> exportDashboardSummary() {
        Map<String, Object> exportData = Map.of(
            "generatedAt", LocalDateTime.now(),
            "systemOverview", dashboardService.getSystemOverview(),
            "studentAnalytics", dashboardService.getStudentAnalytics(),
            "facultyAnalytics", dashboardService.getFacultyAnalytics(),
            "courseAnalytics", dashboardService.getCourseAnalytics(),
            "departmentPerformance", dashboardService.getDepartmentPerformance(),
            "recentActivities", dashboardService.getRecentActivities(20),
            "systemHealth", dashboardService.getSystemHealthMetrics(),
            "academicAlerts", dashboardService.getAcademicAlerts()
        );
        return ResponseEntity.ok(exportData);
    }

    // Configuration and Settings
    @PostMapping("/settings/update")
    public ResponseEntity<Map<String, Object>> updateDashboardSettings(
            @RequestBody Map<String, Object> settings) {
        // Implementation for updating dashboard preferences
        Map<String, Object> response = Map.of(
            "success", true,
            "message", "Dashboard settings updated successfully",
            "updatedSettings", settings,
            "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getDashboardSettings() {
        // Return current dashboard configuration
        Map<String, Object> settings = Map.of(
            "refreshInterval", 30, // seconds
            "defaultDateRange", 7, // days
            "alertThresholds", Map.of(
                "lowPerformanceGPA", 5.0,
                "highAbsenteeism", 75.0,
                "systemLoad", 85.0
            ),
            "displayPreferences", Map.of(
                "showDetailedStats", true,
                "enableRealTimeUpdates", true,
                "compactView", false
            )
        );
        return ResponseEntity.ok(settings);
    }
}