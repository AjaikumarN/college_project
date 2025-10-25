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
public class FacultyDashboardService {

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
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    // Comprehensive Faculty Dashboard Overview
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getFacultyDashboardOverview() {
        Faculty faculty = getCurrentFaculty();
        Map<String, Object> dashboard = new HashMap<>();
        
        // Faculty Profile Information
        Map<String, Object> facultyProfile = new HashMap<>();
        facultyProfile.put("id", faculty.getId());
        facultyProfile.put("name", faculty.getUser().getName());
        facultyProfile.put("email", faculty.getUser().getEmail());
        facultyProfile.put("employeeId", faculty.getEmployeeId());
        facultyProfile.put("designation", faculty.getDesignation());
        facultyProfile.put("status", faculty.getStatus());
        facultyProfile.put("experienceYears", faculty.getExperienceYears());
        facultyProfile.put("isDepartmentHead", faculty.getIsDepartmentHead());
        facultyProfile.put("employmentType", faculty.getEmploymentType());
        
        if (faculty.getDepartment() != null) {
            facultyProfile.put("department", Map.of(
                "id", faculty.getDepartment().getId(),
                "name", faculty.getDepartment().getName(),
                "code", faculty.getDepartment().getCode()
            ));
        }
        
        dashboard.put("facultyProfile", facultyProfile);
        
        // Teaching Load Overview
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        Map<String, Object> teachingLoad = new HashMap<>();
        teachingLoad.put("totalCourses", assignedCourses.size());
        
        // Calculate total credits and enrolled students
        int totalCredits = assignedCourses.stream()
                .mapToInt(course -> course.getCredits() != null ? course.getCredits() : 0)
                .sum();
        teachingLoad.put("totalCredits", totalCredits);
        
        int totalStudents = assignedCourses.stream()
                .mapToInt(course -> (int) enrollmentRepository.countByCourseId(course.getId()))
                .sum();
        teachingLoad.put("totalStudents", totalStudents);
        
        // Course status distribution
        Map<String, Long> courseStatusDistribution = assignedCourses.stream()
                .collect(Collectors.groupingBy(
                    course -> course.getStatus().name(),
                    Collectors.counting()
                ));
        teachingLoad.put("courseStatusDistribution", courseStatusDistribution);
        
        dashboard.put("teachingLoad", teachingLoad);
        
        // Academic Performance Overview
        Map<String, Object> academicPerformance = getAcademicPerformanceOverview(assignedCourses);
        dashboard.put("academicPerformance", academicPerformance);
        
        // Attendance Overview
        Map<String, Object> attendanceOverview = getAttendanceOverview(assignedCourses);
        dashboard.put("attendanceOverview", attendanceOverview);
        
        // Grading Progress
        Map<String, Object> gradingProgress = getGradingProgress(assignedCourses);
        dashboard.put("gradingProgress", gradingProgress);
        
        // Recent Activities
        List<Map<String, Object>> recentActivities = getRecentActivities(assignedCourses, 10);
        dashboard.put("recentActivities", recentActivities);
        
        // Pending Tasks
        Map<String, Object> pendingTasks = getPendingTasks(assignedCourses);
        dashboard.put("pendingTasks", pendingTasks);
        
        // Teaching Insights and Analytics
        Map<String, Object> teachingInsights = getTeachingInsights(assignedCourses);
        dashboard.put("teachingInsights", teachingInsights);
        
        return dashboard;
    }

    // Course-Specific Analytics
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseAnalytics(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic Course Information
        analytics.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode(),
            "credits", course.getCredits(),
            "status", course.getStatus(),
            "type", course.getType(),
            "maxCapacity", course.getMaxCapacity()
        ));
        
        // Enrollment Analytics
        Map<String, Object> enrollmentAnalytics = getEnrollmentAnalytics(courseId);
        analytics.put("enrollmentAnalytics", enrollmentAnalytics);
        
        // Grade Analytics
        Map<String, Object> gradeAnalytics = getGradeAnalytics(courseId);
        analytics.put("gradeAnalytics", gradeAnalytics);
        
        // Attendance Analytics
        Map<String, Object> attendanceAnalytics = getAttendanceAnalytics(courseId);
        analytics.put("attendanceAnalytics", attendanceAnalytics);
        
        // Student Performance Analysis
        List<Map<String, Object>> studentPerformance = getStudentPerformanceAnalysis(courseId);
        analytics.put("studentPerformance", studentPerformance);
        
        // Course Progression Timeline
        Map<String, Object> progressionTimeline = getCourseProgressionTimeline(courseId);
        analytics.put("progressionTimeline", progressionTimeline);
        
        return analytics;
    }

    // Teaching Performance Metrics
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getTeachingPerformanceMetrics() {
        Faculty faculty = getCurrentFaculty();
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Overall Teaching Statistics
        metrics.put("totalCoursesEverTaught", assignedCourses.size());
        
        // Calculate average class performance across all courses
        List<Grade> allGrades = new ArrayList<>();
        for (Course course : assignedCourses) {
            allGrades.addAll(gradeRepository.findByCourseId(course.getId()));
        }
        
        if (!allGrades.isEmpty()) {
            double averageClassPerformance = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            metrics.put("averageClassPerformance", Math.round(averageClassPerformance * 100.0) / 100.0);
            
            // Pass rate calculation
            long passingGrades = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null && g.getNumericGrade() >= 60.0)
                    .count();
            double overallPassRate = (double) passingGrades / allGrades.size() * 100;
            metrics.put("overallPassRate", Math.round(overallPassRate * 100.0) / 100.0);
        } else {
            metrics.put("averageClassPerformance", 0.0);
            metrics.put("overallPassRate", 0.0);
        }
        
        // Calculate overall attendance rate
        List<AttendanceRecord> allAttendance = new ArrayList<>();
        for (Course course : assignedCourses) {
            allAttendance.addAll(attendanceRepository.findByCourseId(course.getId()));
        }
        
        if (!allAttendance.isEmpty()) {
            long totalPresent = allAttendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double overallAttendanceRate = (double) totalPresent / allAttendance.size() * 100;
            metrics.put("overallAttendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        } else {
            metrics.put("overallAttendanceRate", 0.0);
        }
        
        // Course-wise performance comparison
        List<Map<String, Object>> coursePerformanceComparison = new ArrayList<>();
        for (Course course : assignedCourses) {
            Map<String, Object> courseMetrics = new HashMap<>();
            courseMetrics.put("courseId", course.getId());
            courseMetrics.put("courseName", course.getCourseName());
            courseMetrics.put("courseCode", course.getCourseCode());
            
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            if (!courseGrades.isEmpty()) {
                double avgGrade = courseGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                courseMetrics.put("averageGrade", Math.round(avgGrade * 100.0) / 100.0);
                
                long passing = courseGrades.stream()
                        .filter(g -> g.getNumericGrade() != null && g.getNumericGrade() >= 60.0)
                        .count();
                double passRate = (double) passing / courseGrades.size() * 100;
                courseMetrics.put("passRate", Math.round(passRate * 100.0) / 100.0);
            } else {
                courseMetrics.put("averageGrade", 0.0);
                courseMetrics.put("passRate", 0.0);
            }
            
            // Attendance rate for this course
            List<AttendanceRecord> courseAttendance = attendanceRepository.findByCourseId(course.getId());
            if (!courseAttendance.isEmpty()) {
                long present = courseAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendanceRate = (double) present / courseAttendance.size() * 100;
                courseMetrics.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            } else {
                courseMetrics.put("attendanceRate", 0.0);
            }
            
            coursePerformanceComparison.add(courseMetrics);
        }
        
        metrics.put("coursePerformanceComparison", coursePerformanceComparison);
        
        // Teaching effectiveness indicators
        Map<String, Object> effectivenessIndicators = new HashMap<>();
        effectivenessIndicators.put("highPerformingCourses", 
            coursePerformanceComparison.stream()
                .filter(c -> (Double) c.get("averageGrade") >= 80.0)
                .count());
        effectivenessIndicators.put("needsImprovementCourses", 
            coursePerformanceComparison.stream()
                .filter(c -> (Double) c.get("averageGrade") < 60.0)
                .count());
        effectivenessIndicators.put("highAttendanceCourses", 
            coursePerformanceComparison.stream()
                .filter(c -> (Double) c.get("attendanceRate") >= 85.0)
                .count());
        
        metrics.put("effectivenessIndicators", effectivenessIndicators);
        
        return metrics;
    }

    // Student Interaction Analytics
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getStudentInteractionAnalytics() {
        Faculty faculty = getCurrentFaculty();
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Total students taught
        Set<Long> uniqueStudentIds = new HashSet<>();
        for (Course course : assignedCourses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            uniqueStudentIds.addAll(enrollments.stream()
                    .map(e -> e.getStudent().getId())
                    .collect(Collectors.toSet()));
        }
        analytics.put("totalUniqueStudentsTaught", uniqueStudentIds.size());
        
        // Student performance distribution across all courses
        Map<String, Integer> performanceDistribution = new HashMap<>();
        performanceDistribution.put("excellent", 0); // 90-100
        performanceDistribution.put("good", 0); // 80-89
        performanceDistribution.put("satisfactory", 0); // 70-79
        performanceDistribution.put("needsImprovement", 0); // 60-69
        performanceDistribution.put("failing", 0); // <60
        
        for (Long studentId : uniqueStudentIds) {
            List<Grade> studentGrades = new ArrayList<>();
            for (Course course : assignedCourses) {
                studentGrades.addAll(gradeRepository.findByStudentIdAndCourseId(studentId, course.getId()));
            }
            
            if (!studentGrades.isEmpty()) {
                double avgGrade = studentGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                
                if (avgGrade >= 90) {
                    performanceDistribution.merge("excellent", 1, Integer::sum);
                } else if (avgGrade >= 80) {
                    performanceDistribution.merge("good", 1, Integer::sum);
                } else if (avgGrade >= 70) {
                    performanceDistribution.merge("satisfactory", 1, Integer::sum);
                } else if (avgGrade >= 60) {
                    performanceDistribution.merge("needsImprovement", 1, Integer::sum);
                } else {
                    performanceDistribution.merge("failing", 1, Integer::sum);
                }
            }
        }
        
        analytics.put("studentPerformanceDistribution", performanceDistribution);
        
        // Students requiring attention (low performance or attendance)
        List<Map<String, Object>> studentsRequiringAttention = new ArrayList<>();
        for (Long studentId : uniqueStudentIds) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                boolean needsAttention = false;
                List<String> concerns = new ArrayList<>();
                
                // Check grades across all courses
                List<Grade> studentGrades = new ArrayList<>();
                for (Course course : assignedCourses) {
                    studentGrades.addAll(gradeRepository.findByStudentIdAndCourseId(studentId, course.getId()));
                }
                
                if (!studentGrades.isEmpty()) {
                    double avgGrade = studentGrades.stream()
                            .filter(g -> g.getNumericGrade() != null)
                            .mapToDouble(Grade::getNumericGrade)
                            .average()
                            .orElse(0.0);
                    
                    if (avgGrade < 60.0) {
                        needsAttention = true;
                        concerns.add("Low academic performance");
                    }
                }
                
                // Check attendance across all courses
                List<AttendanceRecord> studentAttendance = new ArrayList<>();
                for (Course course : assignedCourses) {
                    studentAttendance.addAll(attendanceRepository.findByStudentIdAndCourseId(studentId, course.getId()));
                }
                
                if (!studentAttendance.isEmpty()) {
                    long presentCount = studentAttendance.stream()
                            .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                            .count();
                    double attendanceRate = (double) presentCount / studentAttendance.size() * 100;
                    
                    if (attendanceRate < 75.0) {
                        needsAttention = true;
                        concerns.add("Low attendance");
                    }
                }
                
                if (needsAttention) {
                    Map<String, Object> studentInfo = new HashMap<>();
                    studentInfo.put("studentId", student.getId());
                    studentInfo.put("studentName", student.getUser().getName());
                    studentInfo.put("studentNumber", student.getStudentId());
                    studentInfo.put("concerns", concerns);
                    studentsRequiringAttention.add(studentInfo);
                }
            }
        }
        
        analytics.put("studentsRequiringAttention", studentsRequiringAttention);
        analytics.put("attentionRequiredCount", studentsRequiringAttention.size());
        
        return analytics;
    }

    // Weekly Teaching Schedule and Analytics
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getWeeklyTeachingAnalytics() {
        Faculty faculty = getCurrentFaculty();
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        
        Map<String, Object> weeklyAnalytics = new HashMap<>();
        
        // Get current week dates
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        weeklyAnalytics.put("weekRange", Map.of(
            "startDate", startOfWeek,
            "endDate", endOfWeek
        ));
        
        // Weekly attendance summary
        Map<String, Object> weeklyAttendance = new HashMap<>();
        int totalWeeklyClasses = 0;
        int totalWeeklyPresent = 0;
        
        for (Course course : assignedCourses) {
            List<AttendanceRecord> weeklyRecords = attendanceRepository
                    .findByCourseIdAndAttendanceDateBetween(course.getId(), startOfWeek, endOfWeek);
            
            totalWeeklyClasses += weeklyRecords.size();
            totalWeeklyPresent += weeklyRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
        }
        
        weeklyAttendance.put("totalClasses", totalWeeklyClasses);
        weeklyAttendance.put("totalPresent", totalWeeklyPresent);
        weeklyAttendance.put("weeklyAttendanceRate", totalWeeklyClasses > 0 ? 
            Math.round((double) totalWeeklyPresent / totalWeeklyClasses * 100.0 * 100.0) / 100.0 : 0.0);
        
        weeklyAnalytics.put("weeklyAttendance", weeklyAttendance);
        
        // Weekly grading activity
        Map<String, Object> weeklyGrading = new HashMap<>();
        int weeklyGradesEntered = 0;
        
        for (Course course : assignedCourses) {
            List<Grade> weeklyGrades = gradeRepository.findByCourseIdAndGradedDateBetween(
                course.getId(), startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59));
            weeklyGradesEntered += weeklyGrades.size();
        }
        
        weeklyGrading.put("gradesEntered", weeklyGradesEntered);
        weeklyAnalytics.put("weeklyGrading", weeklyGrading);
        
        // Daily breakdown
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDay);
            dayData.put("dayOfWeek", currentDay.getDayOfWeek().name());
            
            // Count attendance for this day
            int dayAttendanceRecords = 0;
            int dayPresentRecords = 0;
            
            for (Course course : assignedCourses) {
                List<AttendanceRecord> dayRecords = attendanceRepository
                        .findByCourseIdAndAttendanceDate(course.getId(), currentDay);
                dayAttendanceRecords += dayRecords.size();
                dayPresentRecords += dayRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
            }
            
            dayData.put("totalClasses", dayAttendanceRecords);
            dayData.put("totalPresent", dayPresentRecords);
            dayData.put("attendanceRate", dayAttendanceRecords > 0 ? 
                Math.round((double) dayPresentRecords / dayAttendanceRecords * 100.0 * 100.0) / 100.0 : 0.0);
            
            // Count grades entered for this day
            int dayGrades = 0;
            for (Course course : assignedCourses) {
                List<Grade> dayGradeRecords = gradeRepository.findByCourseIdAndGradedDateBetween(
                    course.getId(), currentDay.atStartOfDay(), currentDay.atTime(23, 59, 59));
                dayGrades += dayGradeRecords.size();
            }
            dayData.put("gradesEntered", dayGrades);
            
            dailyBreakdown.add(dayData);
        }
        
        weeklyAnalytics.put("dailyBreakdown", dailyBreakdown);
        
        return weeklyAnalytics;
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

    private Map<String, Object> getAcademicPerformanceOverview(List<Course> courses) {
        Map<String, Object> performance = new HashMap<>();
        
        List<Grade> allGrades = new ArrayList<>();
        for (Course course : courses) {
            allGrades.addAll(gradeRepository.findByCourseId(course.getId()));
        }
        
        performance.put("totalGradesEntered", allGrades.size());
        
        if (!allGrades.isEmpty()) {
            double averageGrade = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            performance.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
            
            // Grade distribution
            Map<String, Long> gradeDistribution = allGrades.stream()
                    .filter(g -> g.getLetterGrade() != null)
                    .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
            performance.put("gradeDistribution", gradeDistribution);
            
            // Pass rate
            long passingGrades = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null && g.getNumericGrade() >= 60.0)
                    .count();
            double passRate = (double) passingGrades / allGrades.size() * 100;
            performance.put("passRate", Math.round(passRate * 100.0) / 100.0);
        } else {
            performance.put("averageGrade", 0.0);
            performance.put("gradeDistribution", new HashMap<>());
            performance.put("passRate", 0.0);
        }
        
        return performance;
    }

    private Map<String, Object> getAttendanceOverview(List<Course> courses) {
        Map<String, Object> attendance = new HashMap<>();
        
        List<AttendanceRecord> allAttendance = new ArrayList<>();
        for (Course course : courses) {
            allAttendance.addAll(attendanceRepository.findByCourseId(course.getId()));
        }
        
        attendance.put("totalAttendanceRecords", allAttendance.size());
        
        if (!allAttendance.isEmpty()) {
            long presentCount = allAttendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double attendanceRate = (double) presentCount / allAttendance.size() * 100;
            attendance.put("overallAttendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            
            // Status distribution
            Map<String, Long> statusDistribution = allAttendance.stream()
                    .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                    ));
            attendance.put("attendanceStatusDistribution", statusDistribution);
        } else {
            attendance.put("overallAttendanceRate", 0.0);
            attendance.put("attendanceStatusDistribution", new HashMap<>());
        }
        
        return attendance;
    }

    private Map<String, Object> getGradingProgress(List<Course> courses) {
        Map<String, Object> progress = new HashMap<>();
        
        int totalEnrolledStudents = 0;
        int totalGradedStudents = 0;
        
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            totalEnrolledStudents += enrollments.size();
            
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            Set<Long> gradedStudentIds = courseGrades.stream()
                    .map(g -> g.getStudent().getId())
                    .collect(Collectors.toSet());
            totalGradedStudents += gradedStudentIds.size();
        }
        
        progress.put("totalStudentsToGrade", totalEnrolledStudents);
        progress.put("totalStudentsGraded", totalGradedStudents);
        progress.put("gradingCompletionPercentage", totalEnrolledStudents > 0 ? 
            Math.round((double) totalGradedStudents / totalEnrolledStudents * 100.0 * 100.0) / 100.0 : 0.0);
        
        return progress;
    }

    private List<Map<String, Object>> getRecentActivities(List<Course> courses, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent grades
        for (Course course : courses) {
            List<Grade> recentGrades = gradeRepository.findByCourseIdOrderByGradedDateDesc(course.getId())
                    .stream().limit(limit / courses.size() + 1).toList();
            
            for (Grade grade : recentGrades) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "GRADE_ENTERED");
                activity.put("description", "Grade entered for " + grade.getStudent().getUser().getName() + 
                           " in " + course.getCourseName() + ": " + grade.getLetterGrade());
                activity.put("timestamp", grade.getGradeDate());
                activity.put("courseId", course.getId());
                activity.put("courseName", course.getCourseName());
                activities.add(activity);
            }
            
            // Recent attendance
            List<AttendanceRecord> recentAttendance = attendanceRepository
                    .findByCourseIdOrderByCreatedAtDesc(course.getId())
                    .stream().limit(limit / courses.size() + 1).toList();
            
            for (AttendanceRecord record : recentAttendance) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "ATTENDANCE_MARKED");
                activity.put("description", "Attendance marked for " + record.getStudent().getUser().getName() + 
                           " in " + course.getCourseName() + ": " + record.getStatus());
                activity.put("timestamp", record.getCreatedAt());
                activity.put("courseId", course.getId());
                activity.put("courseName", course.getCourseName());
                activities.add(activity);
            }
        }
        
        // Sort by timestamp and limit
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return activities.stream().limit(limit).toList();
    }

    private Map<String, Object> getPendingTasks(List<Course> courses) {
        Map<String, Object> tasks = new HashMap<>();
        
        List<Map<String, Object>> pendingList = new ArrayList<>();
        
        for (Course course : courses) {
            // Check for students without grades
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            Set<Long> gradedStudentIds = courseGrades.stream()
                    .map(g -> g.getStudent().getId())
                    .collect(Collectors.toSet());
            
            long ungradedStudents = enrollments.stream()
                    .filter(e -> !gradedStudentIds.contains(e.getStudent().getId()))
                    .count();
            
            if (ungradedStudents > 0) {
                Map<String, Object> task = new HashMap<>();
                task.put("type", "GRADING_PENDING");
                task.put("courseId", course.getId());
                task.put("courseName", course.getCourseName());
                task.put("description", ungradedStudents + " students need grades");
                task.put("count", ungradedStudents);
                task.put("priority", ungradedStudents > 10 ? "HIGH" : "MEDIUM");
                pendingList.add(task);
            }
            
            // Check for today's attendance not marked
            LocalDate today = LocalDate.now();
            List<AttendanceRecord> todayAttendance = attendanceRepository
                    .findByCourseIdAndAttendanceDate(course.getId(), today);
            
            if (todayAttendance.isEmpty() && enrollments.size() > 0) {
                Map<String, Object> task = new HashMap<>();
                task.put("type", "ATTENDANCE_PENDING");
                task.put("courseId", course.getId());
                task.put("courseName", course.getCourseName());
                task.put("description", "Today's attendance not marked");
                task.put("count", enrollments.size());
                task.put("priority", "HIGH");
                pendingList.add(task);
            }
        }
        
        tasks.put("pendingTasks", pendingList);
        tasks.put("totalPendingTasks", pendingList.size());
        
        return tasks;
    }

    private Map<String, Object> getTeachingInsights(List<Course> courses) {
        Map<String, Object> insights = new HashMap<>();
        
        // Most challenging course (lowest average grade)
        Course mostChallengingCourse = null;
        double lowestAverage = Double.MAX_VALUE;
        
        for (Course course : courses) {
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            if (!courseGrades.isEmpty()) {
                double avgGrade = courseGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                
                if (avgGrade < lowestAverage) {
                    lowestAverage = avgGrade;
                    mostChallengingCourse = course;
                }
            }
        }
        
        if (mostChallengingCourse != null) {
            insights.put("mostChallengingCourse", Map.of(
                "courseId", mostChallengingCourse.getId(),
                "courseName", mostChallengingCourse.getCourseName(),
                "averageGrade", Math.round(lowestAverage * 100.0) / 100.0
            ));
        }
        
        // Best performing course (highest average grade)
        Course bestPerformingCourse = null;
        double highestAverage = 0.0;
        
        for (Course course : courses) {
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            if (!courseGrades.isEmpty()) {
                double avgGrade = courseGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                
                if (avgGrade > highestAverage) {
                    highestAverage = avgGrade;
                    bestPerformingCourse = course;
                }
            }
        }
        
        if (bestPerformingCourse != null) {
            insights.put("bestPerformingCourse", Map.of(
                "courseId", bestPerformingCourse.getId(),
                "courseName", bestPerformingCourse.getCourseName(),
                "averageGrade", Math.round(highestAverage * 100.0) / 100.0
            ));
        }
        
        return insights;
    }

    // Additional helper methods for course analytics
    private Map<String, Object> getEnrollmentAnalytics(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalEnrollments", enrollments.size());
        
        // Enrollment status distribution
        Map<String, Long> statusDistribution = enrollments.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                ));
        analytics.put("enrollmentStatusDistribution", statusDistribution);
        
        return analytics;
    }

    private Map<String, Object> getGradeAnalytics(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalGrades", grades.size());
        
        if (!grades.isEmpty()) {
            // Grade distribution
            Map<String, Long> gradeDistribution = grades.stream()
                    .filter(g -> g.getLetterGrade() != null)
                    .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
            analytics.put("gradeDistribution", gradeDistribution);
            
            // Average grade
            double averageGrade = grades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            analytics.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
        }
        
        return analytics;
    }

    private Map<String, Object> getAttendanceAnalytics(Long courseId) {
        List<AttendanceRecord> attendance = attendanceRepository.findByCourseId(courseId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalAttendanceRecords", attendance.size());
        
        if (!attendance.isEmpty()) {
            // Status distribution
            Map<String, Long> statusDistribution = attendance.stream()
                    .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                    ));
            analytics.put("statusDistribution", statusDistribution);
            
            // Attendance rate
            long presentCount = attendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double attendanceRate = (double) presentCount / attendance.size() * 100;
            analytics.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        }
        
        return analytics;
    }

    private List<Map<String, Object>> getStudentPerformanceAnalysis(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> performance = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Map<String, Object> studentPerf = new HashMap<>();
            
            studentPerf.put("studentId", student.getId());
            studentPerf.put("studentName", student.getUser().getName());
            
            // Grade analysis
            List<Grade> studentGrades = gradeRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!studentGrades.isEmpty()) {
                double avgGrade = studentGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                studentPerf.put("averageGrade", Math.round(avgGrade * 100.0) / 100.0);
            } else {
                studentPerf.put("averageGrade", 0.0);
            }
            
            // Attendance analysis
            List<AttendanceRecord> studentAttendance = attendanceRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!studentAttendance.isEmpty()) {
                long presentCount = studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendanceRate = (double) presentCount / studentAttendance.size() * 100;
                studentPerf.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            } else {
                studentPerf.put("attendanceRate", 0.0);
            }
            
            performance.add(studentPerf);
        }
        
        return performance;
    }

    private Map<String, Object> getCourseProgressionTimeline(Long courseId) {
        Map<String, Object> timeline = new HashMap<>();
        
        // Grade entry timeline
        List<Grade> grades = gradeRepository.findByCourseIdOrderByGradedDateAsc(courseId);
        Map<String, Long> gradingTimeline = grades.stream()
                .collect(Collectors.groupingBy(
                    g -> g.getGradeDate().toLocalDate().toString(),
                    Collectors.counting()
                ));
        timeline.put("gradingTimeline", gradingTimeline);
        
        // Attendance timeline
        List<AttendanceRecord> attendance = attendanceRepository.findByCourseIdOrderByAttendanceDateAsc(courseId);
        Map<String, Long> attendanceTimeline = attendance.stream()
                .collect(Collectors.groupingBy(
                    a -> a.getAttendanceDate().toString(),
                    Collectors.counting()
                ));
        timeline.put("attendanceTimeline", attendanceTimeline);
        
        return timeline;
    }
}