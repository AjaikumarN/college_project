package com.college.backend.service;

import com.college.backend.model.*;
import com.college.backend.repository.*;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyAttendanceManagementService {

    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepository;

    // Attendance Management Overview
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getAttendanceManagementOverview() {
        Faculty faculty = getCurrentFaculty();
        Map<String, Object> overview = new HashMap<>();
        
        // Get all courses taught by this faculty
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        overview.put("totalCoursesTeaching", assignedCourses.size());
        
        // Calculate total attendance records
        int totalAttendanceRecords = 0;
        int totalStudentsTracked = 0;
        
        for (Course course : assignedCourses) {
            List<AttendanceRecord> courseAttendance = attendanceRepository.findByCourseId(course.getId());
            totalAttendanceRecords += courseAttendance.size();
            
            Set<Long> uniqueStudents = courseAttendance.stream()
                    .map(a -> a.getStudent().getId())
                    .collect(Collectors.toSet());
            totalStudentsTracked += uniqueStudents.size();
        }
        
        overview.put("totalAttendanceRecords", totalAttendanceRecords);
        overview.put("totalStudentsTracked", totalStudentsTracked);
        
        // Calculate overall attendance statistics
        if (totalAttendanceRecords > 0) {
            long totalPresent = 0;
            for (Course course : assignedCourses) {
                List<AttendanceRecord> courseAttendance = attendanceRepository.findByCourseId(course.getId());
                totalPresent += courseAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
            }
            
            double overallAttendanceRate = (double) totalPresent / totalAttendanceRecords * 100;
            overview.put("overallAttendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        } else {
            overview.put("overallAttendanceRate", 0.0);
        }
        
        // Recent attendance activity
        List<Map<String, Object>> recentActivity = getRecentAttendanceActivity(assignedCourses, 10);
        overview.put("recentAttendanceActivity", recentActivity);
        
        // Attendance alerts (students with low attendance)
        List<Map<String, Object>> attendanceAlerts = getAttendanceAlerts(assignedCourses);
        overview.put("attendanceAlerts", attendanceAlerts);
        
        // Course-wise attendance summary
        List<Map<String, Object>> courseAttendanceSummary = new ArrayList<>();
        for (Course course : assignedCourses) {
            Map<String, Object> courseSummary = getCourseAttendanceSummary(course.getId());
            courseAttendanceSummary.add(courseSummary);
        }
        overview.put("courseAttendanceSummary", courseAttendanceSummary);
        
        return overview;
    }

    // Mark Attendance for a Session
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> markSessionAttendance(Long courseId, LocalDate attendanceDate, 
                                                   List<Map<String, Object>> attendanceData) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> result = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<AttendanceRecord> savedRecords = new ArrayList<>();
        
        for (Map<String, Object> attendance : attendanceData) {
            try {
                Long studentId = Long.valueOf(attendance.get("studentId").toString());
                String statusStr = (String) attendance.get("status");
                String remarks = (String) attendance.get("remarks");
                
                AttendanceRecord.AttendanceStatus status = AttendanceRecord.AttendanceStatus.valueOf(statusStr);
                
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
                
                // Verify student is enrolled in the course
                enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in course"));
                
                // Check if attendance already exists for this date
                Optional<AttendanceRecord> existingRecord = attendanceRepository
                        .findByStudentIdAndCourseIdAndAttendanceDate(studentId, courseId, attendanceDate);
                
                AttendanceRecord record;
                if (existingRecord.isPresent()) {
                    // Update existing record
                    record = existingRecord.get();
                    record.setStatus(status);
                    record.setRemarks(remarks);
                    record.setUpdatedAt(LocalDateTime.now());
                } else {
                    // Create new record
                    record = new AttendanceRecord();
                    record.setStudent(student);
                    record.setCourse(course);
                    record.setAttendanceDate(attendanceDate);
                    record.setStatus(status);
                    record.setRemarks(remarks);
                    record.setCreatedAt(LocalDateTime.now());
                }
                
                AttendanceRecord savedRecord = attendanceRepository.save(record);
                savedRecords.add(savedRecord);
                successful.add("Attendance marked for " + student.getUser().getName() + ": " + status);
                
            } catch (Exception e) {
                failed.add("Failed to mark attendance: " + e.getMessage());
            }
        }
        
        result.put("successful", successful);
        result.put("failed", failed);
        result.put("successCount", successful.size());
        result.put("failCount", failed.size());
        result.put("attendanceDate", attendanceDate);
        result.put("course", course.getCourseName());
        result.put("markedBy", faculty.getUser().getName());
        result.put("markedAt", LocalDateTime.now());
        
        return result;
    }

    // Get Attendance for a Specific Date and Course
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getSessionAttendance(Long courseId, LocalDate attendanceDate) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> result = new HashMap<>();
        result.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode()
        ));
        result.put("attendanceDate", attendanceDate);
        
        // Get all enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> attendanceList = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Map<String, Object> attendanceInfo = new HashMap<>();
            
            attendanceInfo.put("studentId", student.getId());
            attendanceInfo.put("studentName", student.getUser().getName());
            attendanceInfo.put("studentNumber", student.getStudentId());
            attendanceInfo.put("email", student.getUser().getEmail());
            
            // Check if attendance exists for this date
            Optional<AttendanceRecord> attendanceRecord = attendanceRepository
                    .findByStudentIdAndCourseIdAndAttendanceDate(student.getId(), courseId, attendanceDate);
            
            if (attendanceRecord.isPresent()) {
                AttendanceRecord record = attendanceRecord.get();
                attendanceInfo.put("status", record.getStatus());
                attendanceInfo.put("remarks", record.getRemarks());
                attendanceInfo.put("markedAt", record.getCreatedAt());
                attendanceInfo.put("isMarked", true);
            } else {
                attendanceInfo.put("status", null);
                attendanceInfo.put("remarks", null);
                attendanceInfo.put("markedAt", null);
                attendanceInfo.put("isMarked", false);
            }
            
            attendanceList.add(attendanceInfo);
        }
        
        // Sort by student name
        attendanceList.sort((a, b) -> ((String) a.get("studentName")).compareTo((String) b.get("studentName")));
        
        result.put("attendanceList", attendanceList);
        result.put("totalStudents", attendanceList.size());
        
        // Calculate attendance statistics for this session
        long presentCount = attendanceList.stream()
                .filter(a -> AttendanceRecord.AttendanceStatus.PRESENT.equals(a.get("status")))
                .count();
        long absentCount = attendanceList.stream()
                .filter(a -> AttendanceRecord.AttendanceStatus.ABSENT.equals(a.get("status")))
                .count();
        long lateCount = attendanceList.stream()
                .filter(a -> AttendanceRecord.AttendanceStatus.LATE.equals(a.get("status")))
                .count();
        long notMarked = attendanceList.stream()
                .filter(a -> a.get("status") == null)
                .count();
        
        result.put("sessionStatistics", Map.of(
            "present", presentCount,
            "absent", absentCount,
            "late", lateCount,
            "notMarked", notMarked,
            "attendanceRate", attendanceList.size() > 0 ? 
                Math.round((double) presentCount / attendanceList.size() * 100.0 * 100.0) / 100.0 : 0.0
        ));
        
        return result;
    }

    // Get Student Attendance History
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getStudentAttendanceHistory(Long courseId, Long studentId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Verify student enrollment
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in this course"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("student", Map.of(
            "id", student.getId(),
            "name", student.getUser().getName(),
            "studentNumber", student.getStudentId(),
            "email", student.getUser().getEmail()
        ));
        result.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode()
        ));
        
        // Get attendance history
        List<AttendanceRecord> attendanceHistory = attendanceRepository
                .findByStudentIdAndCourseIdOrderByAttendanceDateDesc(studentId, courseId);
        
        List<Map<String, Object>> attendanceDetails = new ArrayList<>();
        for (AttendanceRecord record : attendanceHistory) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", record.getId());
            detail.put("date", record.getAttendanceDate());
            detail.put("status", record.getStatus());
            detail.put("remarks", record.getRemarks());
            detail.put("markedAt", record.getCreatedAt());
            attendanceDetails.add(detail);
        }
        
        result.put("attendanceHistory", attendanceDetails);
        result.put("totalRecords", attendanceDetails.size());
        
        // Calculate statistics
        if (!attendanceHistory.isEmpty()) {
            long presentCount = attendanceHistory.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            long absentCount = attendanceHistory.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                    .count();
            long lateCount = attendanceHistory.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                    .count();
            
            double attendancePercentage = (double) presentCount / attendanceHistory.size() * 100;
            
            result.put("attendanceStatistics", Map.of(
                "totalClasses", attendanceHistory.size(),
                "present", presentCount,
                "absent", absentCount,
                "late", lateCount,
                "attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0
            ));
        } else {
            result.put("attendanceStatistics", Map.of(
                "totalClasses", 0,
                "present", 0,
                "absent", 0,
                "late", 0,
                "attendancePercentage", 0.0
            ));
        }
        
        return result;
    }

    // Get Course Attendance Analytics
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseAttendanceAnalytics(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode()
        ));
        
        List<AttendanceRecord> allAttendance = attendanceRepository.findByCourseId(courseId);
        analytics.put("totalAttendanceRecords", allAttendance.size());
        
        if (allAttendance.isEmpty()) {
            analytics.put("message", "No attendance records found for this course");
            return analytics;
        }
        
        // Overall attendance statistics
        Map<String, Long> statusDistribution = allAttendance.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getStatus().name(),
                    Collectors.counting()
                ));
        analytics.put("attendanceStatusDistribution", statusDistribution);
        
        // Calculate overall attendance rate
        long presentCount = allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();
        double overallAttendanceRate = (double) presentCount / allAttendance.size() * 100;
        analytics.put("overallAttendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        
        // Daily attendance trends
        Map<String, Map<String, Long>> dailyTrends = allAttendance.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getAttendanceDate().toString(),
                    Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                    )
                ));
        analytics.put("dailyAttendanceTrends", dailyTrends);
        
        // Student-wise attendance summary
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> studentAttendanceSummary = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<AttendanceRecord> studentAttendance = attendanceRepository
                    .findByStudentIdAndCourseId(student.getId(), courseId);
            
            Map<String, Object> studentSummary = new HashMap<>();
            studentSummary.put("studentId", student.getId());
            studentSummary.put("studentName", student.getUser().getName());
            studentSummary.put("studentNumber", student.getStudentId());
            
            if (!studentAttendance.isEmpty()) {
                long studentPresentCount = studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double studentAttendanceRate = (double) studentPresentCount / studentAttendance.size() * 100;
                
                studentSummary.put("totalClasses", studentAttendance.size());
                studentSummary.put("present", studentPresentCount);
                studentSummary.put("absent", studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                        .count());
                studentSummary.put("late", studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                        .count());
                studentSummary.put("attendancePercentage", Math.round(studentAttendanceRate * 100.0) / 100.0);
            } else {
                studentSummary.put("totalClasses", 0);
                studentSummary.put("present", 0);
                studentSummary.put("absent", 0);
                studentSummary.put("late", 0);
                studentSummary.put("attendancePercentage", 0.0);
            }
            
            studentAttendanceSummary.add(studentSummary);
        }
        
        // Sort by attendance percentage (lowest first for attention)
        studentAttendanceSummary.sort((a, b) -> 
            Double.compare((Double) a.get("attendancePercentage"), (Double) b.get("attendancePercentage")));
        
        analytics.put("studentAttendanceSummary", studentAttendanceSummary);
        
        // Identify students with low attendance (below 75%)
        List<Map<String, Object>> lowAttendanceStudents = studentAttendanceSummary.stream()
                .filter(s -> (Double) s.get("attendancePercentage") < 75.0)
                .collect(Collectors.toList());
        analytics.put("lowAttendanceStudents", lowAttendanceStudents);
        analytics.put("lowAttendanceCount", lowAttendanceStudents.size());
        
        return analytics;
    }

    // Generate Attendance Reports
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> generateAttendanceReport(Long courseId, String reportType, 
                                                       LocalDate startDate, LocalDate endDate) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", reportType);
        report.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode()
        ));
        report.put("dateRange", Map.of(
            "startDate", startDate,
            "endDate", endDate
        ));
        report.put("generatedBy", faculty.getUser().getName());
        report.put("generatedAt", LocalDateTime.now());
        
        // Get attendance records within date range
        List<AttendanceRecord> attendanceRecords = attendanceRepository
                .findByCourseIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(courseId, startDate, endDate);
        
        switch (reportType.toLowerCase()) {
            case "summary":
                report.putAll(generateSummaryReport(attendanceRecords, courseId));
                break;
            case "detailed":
                report.putAll(generateDetailedReport(attendanceRecords, courseId));
                break;
            case "defaulters":
                report.putAll(generateDefaultersReport(courseId, startDate, endDate));
                break;
            case "daily":
                report.putAll(generateDailyReport(attendanceRecords));
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
        
        return report;
    }

    // Helper Methods
    private Faculty getCurrentFaculty() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return facultyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));
    }

    private Course validateCourseAccess(Long courseId, Faculty faculty) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        if (!course.getInstructor().getId().equals(faculty.getId())) {
            throw new IllegalArgumentException("Access denied: You are not assigned to this course");
        }
        
        return course;
    }

    private Map<String, Object> getCourseAttendanceSummary(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return new HashMap<>();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("courseId", course.getId());
        summary.put("courseName", course.getCourseName());
        summary.put("courseCode", course.getCourseCode());
        
        List<AttendanceRecord> courseAttendance = attendanceRepository.findByCourseId(courseId);
        summary.put("totalRecords", courseAttendance.size());
        
        if (!courseAttendance.isEmpty()) {
            long presentCount = courseAttendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double attendanceRate = (double) presentCount / courseAttendance.size() * 100;
            summary.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        } else {
            summary.put("attendanceRate", 0.0);
        }
        
        return summary;
    }

    private List<Map<String, Object>> getRecentAttendanceActivity(List<Course> courses, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Course course : courses) {
            List<AttendanceRecord> recentRecords = attendanceRepository
                    .findByCourseIdOrderByCreatedAtDesc(course.getId())
                    .stream().limit(limit / courses.size() + 1).toList();
            
            for (AttendanceRecord record : recentRecords) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("courseId", course.getId());
                activity.put("courseName", course.getCourseName());
                activity.put("studentName", record.getStudent().getUser().getName());
                activity.put("attendanceDate", record.getAttendanceDate());
                activity.put("status", record.getStatus());
                activity.put("markedAt", record.getCreatedAt());
                activities.add(activity);
            }
        }
        
        // Sort by creation time and limit
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("markedAt");
            LocalDateTime timeB = (LocalDateTime) b.get("markedAt");
            return timeB.compareTo(timeA);
        });
        
        return activities.stream().limit(limit).toList();
    }

    private List<Map<String, Object>> getAttendanceAlerts(List<Course> courses) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            
            for (Enrollment enrollment : enrollments) {
                Student student = enrollment.getStudent();
                List<AttendanceRecord> studentAttendance = attendanceRepository
                        .findByStudentIdAndCourseId(student.getId(), course.getId());
                
                if (!studentAttendance.isEmpty()) {
                    long presentCount = studentAttendance.stream()
                            .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                            .count();
                    double attendancePercentage = (double) presentCount / studentAttendance.size() * 100;
                    
                    if (attendancePercentage < 75.0) { // Alert threshold
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("studentId", student.getId());
                        alert.put("studentName", student.getUser().getName());
                        alert.put("courseId", course.getId());
                        alert.put("courseName", course.getCourseName());
                        alert.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
                        alert.put("totalClasses", studentAttendance.size());
                        alert.put("present", presentCount);
                        alert.put("severity", attendancePercentage < 50 ? "CRITICAL" : "WARNING");
                        alerts.add(alert);
                    }
                }
            }
        }
        
        // Sort by attendance percentage (lowest first)
        alerts.sort((a, b) -> Double.compare((Double) a.get("attendancePercentage"), (Double) b.get("attendancePercentage")));
        
        return alerts;
    }

    // Report Generation Helper Methods
    private Map<String, Object> generateSummaryReport(List<AttendanceRecord> records, Long courseId) {
        Map<String, Object> summary = new HashMap<>();
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        summary.put("totalStudents", enrollments.size());
        summary.put("totalRecords", records.size());
        
        if (!records.isEmpty()) {
            Map<String, Long> statusDistribution = records.stream()
                    .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                    ));
            summary.put("statusDistribution", statusDistribution);
            
            long presentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double overallRate = (double) presentCount / records.size() * 100;
            summary.put("overallAttendanceRate", Math.round(overallRate * 100.0) / 100.0);
        }
        
        return summary;
    }

    private Map<String, Object> generateDetailedReport(List<AttendanceRecord> records, Long courseId) {
        Map<String, Object> detailed = new HashMap<>();
        
        // Group by student
        Map<Long, List<AttendanceRecord>> studentRecords = records.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        
        List<Map<String, Object>> studentDetails = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceRecord>> entry : studentRecords.entrySet()) {
            Student student = studentRepository.findById(entry.getKey()).orElse(null);
            if (student != null) {
                List<AttendanceRecord> studentAttendance = entry.getValue();
                
                Map<String, Object> studentDetail = new HashMap<>();
                studentDetail.put("studentId", student.getId());
                studentDetail.put("studentName", student.getUser().getName());
                studentDetail.put("studentNumber", student.getStudentId());
                
                long presentCount = studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendanceRate = (double) presentCount / studentAttendance.size() * 100;
                
                studentDetail.put("totalClasses", studentAttendance.size());
                studentDetail.put("present", presentCount);
                studentDetail.put("absent", studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                        .count());
                studentDetail.put("late", studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                        .count());
                studentDetail.put("attendancePercentage", Math.round(attendanceRate * 100.0) / 100.0);
                
                studentDetails.add(studentDetail);
            }
        }
        
        detailed.put("studentDetails", studentDetails);
        return detailed;
    }

    private Map<String, Object> generateDefaultersReport(Long courseId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> defaulters = new HashMap<>();
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> defaultersList = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<AttendanceRecord> studentAttendance = attendanceRepository
                    .findByStudentIdAndCourseIdAndAttendanceDateBetween(student.getId(), courseId, startDate, endDate);
            
            if (!studentAttendance.isEmpty()) {
                long presentCount = studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendanceRate = (double) presentCount / studentAttendance.size() * 100;
                
                if (attendanceRate < 75.0) { // Defaulter threshold
                    Map<String, Object> defaulter = new HashMap<>();
                    defaulter.put("studentId", student.getId());
                    defaulter.put("studentName", student.getUser().getName());
                    defaulter.put("studentNumber", student.getStudentId());
                    defaulter.put("email", student.getUser().getEmail());
                    defaulter.put("totalClasses", studentAttendance.size());
                    defaulter.put("present", presentCount);
                    defaulter.put("attendancePercentage", Math.round(attendanceRate * 100.0) / 100.0);
                    defaultersList.add(defaulter);
                }
            }
        }
        
        // Sort by attendance percentage
        defaultersList.sort((a, b) -> Double.compare((Double) a.get("attendancePercentage"), (Double) b.get("attendancePercentage")));
        
        defaulters.put("defaultersList", defaultersList);
        defaulters.put("totalDefaulters", defaultersList.size());
        return defaulters;
    }

    private Map<String, Object> generateDailyReport(List<AttendanceRecord> records) {
        Map<String, Object> daily = new HashMap<>();
        
        Map<String, List<AttendanceRecord>> dailyRecords = records.stream()
                .collect(Collectors.groupingBy(a -> a.getAttendanceDate().toString()));
        
        List<Map<String, Object>> dailyDetails = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceRecord>> entry : dailyRecords.entrySet()) {
            List<AttendanceRecord> dayRecords = entry.getValue();
            
            Map<String, Object> dayDetail = new HashMap<>();
            dayDetail.put("date", entry.getKey());
            dayDetail.put("totalStudents", dayRecords.size());
            
            long present = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            
            dayDetail.put("present", present);
            dayDetail.put("absent", dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                    .count());
            dayDetail.put("late", dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                    .count());
            
            double attendanceRate = (double) present / dayRecords.size() * 100;
            dayDetail.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            
            dailyDetails.add(dayDetail);
        }
        
        // Sort by date
        dailyDetails.sort((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")));
        
        daily.put("dailyDetails", dailyDetails);
        return daily;
    }
}