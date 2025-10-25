package com.college.backend.service;

import com.college.backend.model.*;
import com.college.backend.repository.*;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyStudentManagementService {

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

    // Student Overview for Faculty
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getStudentManagementOverview() {
        Faculty faculty = getCurrentFaculty();
        Map<String, Object> overview = new HashMap<>();
        
        // Get all courses taught by this faculty
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        overview.put("totalCoursesTeaching", assignedCourses.size());
        
        // Get all students across all courses
        Set<Student> allStudents = new HashSet<>();
        Map<Long, List<String>> studentCourseMap = new HashMap<>();
        
        for (Course course : assignedCourses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            for (Enrollment enrollment : enrollments) {
                Student student = enrollment.getStudent();
                allStudents.add(student);
                studentCourseMap.computeIfAbsent(student.getId(), k -> new ArrayList<>())
                        .add(course.getCourseName());
            }
        }
        
        overview.put("totalStudentsTeaching", allStudents.size());
        
        // Student status distribution
        Map<String, Long> statusDistribution = allStudents.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getStatus().name(),
                    Collectors.counting()
                ));
        overview.put("studentStatusDistribution", statusDistribution);
        
        // Performance distribution
        Map<String, Integer> performanceDistribution = new HashMap<>();
        performanceDistribution.put("highPerformers", 0);
        performanceDistribution.put("averagePerformers", 0);
        performanceDistribution.put("lowPerformers", 0);
        
        for (Student student : allStudents) {
            if (student.getCgpa() >= 8.5) {
                performanceDistribution.merge("highPerformers", 1, Integer::sum);
            } else if (student.getCgpa() >= 6.0) {
                performanceDistribution.merge("averagePerformers", 1, Integer::sum);
            } else {
                performanceDistribution.merge("lowPerformers", 1, Integer::sum);
            }
        }
        overview.put("performanceDistribution", performanceDistribution);
        
        // Recent student activities
        List<Map<String, Object>> recentActivities = getRecentStudentActivities(assignedCourses);
        overview.put("recentStudentActivities", recentActivities);
        
        return overview;
    }

    // Get Students by Course
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getStudentsByCourse(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> result = new HashMap<>();
        result.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode(),
            "credits", course.getCredits()
        ));
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> studentDetails = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Map<String, Object> studentInfo = new HashMap<>();
            
            // Basic student information
            studentInfo.put("studentId", student.getId());
            studentInfo.put("studentNumber", student.getStudentId());
            studentInfo.put("name", student.getUser().getName());
            studentInfo.put("email", student.getUser().getEmail());
            studentInfo.put("phone", student.getUser().getPhone());
            studentInfo.put("status", student.getStatus());
            studentInfo.put("cgpa", student.getCgpa());
            studentInfo.put("academicYear", student.getAcademicYear());
            studentInfo.put("semester", student.getSemester());
            
            // Enrollment details
            studentInfo.put("enrollmentDate", enrollment.getEnrollmentDate());
            studentInfo.put("enrollmentStatus", enrollment.getStatus());
            studentInfo.put("currentGrade", enrollment.getCurrentGrade());
            
            // Course-specific performance
            Optional<Grade> latestGrade = gradeRepository.findByStudentIdAndCourseIdOrderByGradedDateDesc(student.getId(), courseId)
                    .stream().findFirst();
            if (latestGrade.isPresent()) {
                studentInfo.put("latestGrade", Map.of(
                    "letterGrade", latestGrade.get().getLetterGrade(),
                    "numericGrade", latestGrade.get().getNumericGrade(),
                    "gradeDate", latestGrade.get().getGradeDate(),
                    "comments", latestGrade.get().getComments() != null ? latestGrade.get().getComments() : ""
                ));
            } else {
                studentInfo.put("latestGrade", null);
            }
            
            // Attendance summary
            List<AttendanceRecord> attendanceRecords = attendanceRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!attendanceRecords.isEmpty()) {
                long presentCount = attendanceRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendancePercentage = (double) presentCount / attendanceRecords.size() * 100;
                studentInfo.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
                studentInfo.put("totalClasses", attendanceRecords.size());
                studentInfo.put("classesAttended", presentCount);
            } else {
                studentInfo.put("attendancePercentage", 0.0);
                studentInfo.put("totalClasses", 0);
                studentInfo.put("classesAttended", 0);
            }
            
            studentDetails.add(studentInfo);
        }
        
        // Sort students by name
        studentDetails.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
        
        result.put("students", studentDetails);
        result.put("totalEnrolled", studentDetails.size());
        
        return result;
    }

    // Student Progress Tracking
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getStudentProgressInCourse(Long studentId, Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Verify student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in this course"));
        
        Map<String, Object> progress = new HashMap<>();
        
        // Basic student and course info
        progress.put("student", Map.of(
            "id", student.getId(),
            "name", student.getUser().getName(),
            "studentNumber", student.getStudentId(),
            "email", student.getUser().getEmail(),
            "cgpa", student.getCgpa(),
            "status", student.getStatus()
        ));
        
        progress.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode(),
            "credits", course.getCredits()
        ));
        
        progress.put("enrollment", Map.of(
            "enrollmentDate", enrollment.getEnrollmentDate(),
            "status", enrollment.getStatus(),
            "currentGrade", enrollment.getCurrentGrade()
        ));
        
        // Grade history
        List<Grade> gradeHistory = gradeRepository.findByStudentIdAndCourseIdOrderByGradedDateDesc(studentId, courseId);
        List<Map<String, Object>> gradeDetails = new ArrayList<>();
        for (Grade grade : gradeHistory) {
            Map<String, Object> gradeInfo = new HashMap<>();
            gradeInfo.put("id", grade.getId());
            gradeInfo.put("letterGrade", grade.getLetterGrade());
            gradeInfo.put("numericGrade", grade.getNumericGrade());
            gradeInfo.put("gradeDate", grade.getGradeDate());
            gradeInfo.put("gradeType", grade.getGradeType());
            gradeInfo.put("maxPoints", grade.getMaxPoints());
            gradeInfo.put("pointsEarned", grade.getPointsEarned());
            gradeInfo.put("comments", grade.getComments());
            gradeDetails.add(gradeInfo);
        }
        progress.put("gradeHistory", gradeDetails);
        
        // Attendance history
        List<AttendanceRecord> attendanceHistory = attendanceRepository.findByStudentIdAndCourseIdOrderByAttendanceDateDesc(studentId, courseId);
        List<Map<String, Object>> attendanceDetails = new ArrayList<>();
        for (AttendanceRecord record : attendanceHistory) {
            Map<String, Object> attendanceInfo = new HashMap<>();
            attendanceInfo.put("id", record.getId());
            attendanceInfo.put("date", record.getAttendanceDate());
            attendanceInfo.put("status", record.getStatus());
            attendanceInfo.put("remarks", record.getRemarks());
            attendanceDetails.add(attendanceInfo);
        }
        progress.put("attendanceHistory", attendanceDetails);
        
        // Performance summary
        Map<String, Object> summary = new HashMap<>();
        if (!gradeHistory.isEmpty()) {
            double averageGrade = gradeHistory.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            summary.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
            summary.put("totalGrades", gradeHistory.size());
            
            Grade latestGrade = gradeHistory.get(0);
            summary.put("latestGrade", latestGrade.getLetterGrade());
            summary.put("latestNumericGrade", latestGrade.getNumericGrade());
        } else {
            summary.put("averageGrade", 0.0);
            summary.put("totalGrades", 0);
            summary.put("latestGrade", "N/A");
            summary.put("latestNumericGrade", null);
        }
        
        if (!attendanceHistory.isEmpty()) {
            long presentCount = attendanceHistory.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double attendancePercentage = (double) presentCount / attendanceHistory.size() * 100;
            summary.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
            summary.put("totalClasses", attendanceHistory.size());
            summary.put("classesAttended", presentCount);
        } else {
            summary.put("attendancePercentage", 0.0);
            summary.put("totalClasses", 0);
            summary.put("classesAttended", 0);
        }
        
        progress.put("performanceSummary", summary);
        
        return progress;
    }

    // Student Communication Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> sendMessageToStudent(Long studentId, Long courseId, String subject, String message) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Verify student is enrolled in the course
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in this course"));
        
        // For now, we'll return a success response
        // In a real implementation, this would integrate with an email or messaging service
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Message sent successfully");
        result.put("recipient", Map.of(
            "studentName", student.getUser().getName(),
            "studentEmail", student.getUser().getEmail()
        ));
        result.put("sender", Map.of(
            "facultyName", faculty.getUser().getName(),
            "facultyEmail", faculty.getUser().getEmail()
        ));
        result.put("messageDetails", Map.of(
            "subject", subject,
            "message", message,
            "course", course.getCourseName(),
            "sentAt", LocalDateTime.now()
        ));
        
        return result;
    }

    // Bulk Student Operations
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> performBulkStudentOperation(Long courseId, List<Long> studentIds, String operation, Map<String, Object> parameters) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> result = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (Long studentId : studentIds) {
            try {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
                
                // Verify student is enrolled in the course
                Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in course"));
                
                switch (operation.toLowerCase()) {
                    case "send_notification":
                        // Send notification to student
                        successful.add("Notification sent to " + student.getUser().getName());
                        break;
                    case "mark_attendance":
                        // Mark attendance for today
                        AttendanceRecord.AttendanceStatus status = AttendanceRecord.AttendanceStatus.valueOf((String) parameters.get("status"));
                        markAttendanceForStudent(student, course, LocalDate.now(), status);
                        successful.add("Attendance marked for " + student.getUser().getName());
                        break;
                    case "update_enrollment_status":
                        // Update enrollment status
                        Enrollment.EnrollmentStatus newStatus = Enrollment.EnrollmentStatus.valueOf((String) parameters.get("enrollmentStatus"));
                        enrollment.setStatus(newStatus);
                        enrollmentRepository.save(enrollment);
                        successful.add("Enrollment status updated for " + student.getUser().getName());
                        break;
                    default:
                        failed.add("Unknown operation for student: " + student.getUser().getName());
                }
            } catch (Exception e) {
                failed.add("Failed for student ID " + studentId + ": " + e.getMessage());
            }
        }
        
        result.put("successful", successful);
        result.put("failed", failed);
        result.put("successCount", successful.size());
        result.put("failCount", failed.size());
        result.put("operation", operation);
        result.put("course", course.getCourseName());
        result.put("executedBy", faculty.getUser().getName());
        result.put("executedAt", LocalDateTime.now());
        
        return result;
    }

    // Student Performance Analytics for Course
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseStudentAnalytics(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> analytics = new HashMap<>();
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        analytics.put("totalStudents", enrollments.size());
        
        // Enrollment status distribution
        Map<String, Long> enrollmentStatusDistribution = enrollments.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                ));
        analytics.put("enrollmentStatusDistribution", enrollmentStatusDistribution);
        
        // Performance distribution
        Map<String, Integer> performanceDistribution = new HashMap<>();
        performanceDistribution.put("excellent", 0); // >90
        performanceDistribution.put("good", 0); // 80-90
        performanceDistribution.put("average", 0); // 60-80
        performanceDistribution.put("below_average", 0); // <60
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<Grade> studentGrades = gradeRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            
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
                } else if (avgGrade >= 60) {
                    performanceDistribution.merge("average", 1, Integer::sum);
                } else {
                    performanceDistribution.merge("below_average", 1, Integer::sum);
                }
            }
        }
        analytics.put("performanceDistribution", performanceDistribution);
        
        // Attendance analytics
        List<AttendanceRecord> allAttendanceRecords = attendanceRepository.findByCourseId(courseId);
        if (!allAttendanceRecords.isEmpty()) {
            long totalPresent = allAttendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double overallAttendanceRate = (double) totalPresent / allAttendanceRecords.size() * 100;
            analytics.put("overallAttendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        } else {
            analytics.put("overallAttendanceRate", 0.0);
        }
        
        // Top performers
        List<Map<String, Object>> topPerformers = getTopPerformingStudents(courseId, 5);
        analytics.put("topPerformers", topPerformers);
        
        // Students needing attention (low performance or attendance)
        List<Map<String, Object>> studentsNeedingAttention = getStudentsNeedingAttention(courseId);
        analytics.put("studentsNeedingAttention", studentsNeedingAttention);
        
        return analytics;
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

    private List<Map<String, Object>> getRecentStudentActivities(List<Course> courses) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Course course : courses) {
            // Recent enrollments
            List<Enrollment> recentEnrollments = enrollmentRepository.findByCourseIdOrderByEnrollmentDateDesc(course.getId())
                    .stream().limit(3).toList();
            
            for (Enrollment enrollment : recentEnrollments) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "ENROLLMENT");
                activity.put("description", enrollment.getStudent().getUser().getName() + " enrolled in " + course.getCourseName());
                activity.put("timestamp", enrollment.getEnrollmentDate());
                activity.put("course", course.getCourseName());
                activities.add(activity);
            }
        }
        
        // Sort by timestamp and limit
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return activities.stream().limit(10).toList();
    }

    private void markAttendanceForStudent(Student student, Course course, LocalDate date, AttendanceRecord.AttendanceStatus status) {
        // Check if attendance already exists for this date
        Optional<AttendanceRecord> existingRecord = attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(
            student.getId(), course.getId(), date);
        
        if (existingRecord.isPresent()) {
            // Update existing record
            AttendanceRecord record = existingRecord.get();
            record.setStatus(status);
            record.setUpdatedAt(LocalDateTime.now());
            attendanceRepository.save(record);
        } else {
            // Create new record
            AttendanceRecord newRecord = new AttendanceRecord();
            newRecord.setStudent(student);
            newRecord.setCourse(course);
            newRecord.setAttendanceDate(date);
            newRecord.setStatus(status);
            newRecord.setCreatedAt(LocalDateTime.now());
            attendanceRepository.save(newRecord);
        }
    }

    private List<Map<String, Object>> getTopPerformingStudents(Long courseId, int limit) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> performers = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<Grade> grades = gradeRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            
            if (!grades.isEmpty()) {
                double avgGrade = grades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                
                Map<String, Object> performer = new HashMap<>();
                performer.put("studentId", student.getId());
                performer.put("studentName", student.getUser().getName());
                performer.put("studentNumber", student.getStudentId());
                performer.put("averageGrade", Math.round(avgGrade * 100.0) / 100.0);
                performers.add(performer);
            }
        }
        
        // Sort by average grade and limit
        performers.sort((a, b) -> Double.compare((Double) b.get("averageGrade"), (Double) a.get("averageGrade")));
        return performers.stream().limit(limit).toList();
    }

    private List<Map<String, Object>> getStudentsNeedingAttention(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> needingAttention = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            boolean needsAttention = false;
            List<String> reasons = new ArrayList<>();
            
            // Check grade performance
            List<Grade> grades = gradeRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!grades.isEmpty()) {
                double avgGrade = grades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                
                if (avgGrade < 60.0) {
                    needsAttention = true;
                    reasons.add("Low grade performance (Average: " + Math.round(avgGrade * 100.0) / 100.0 + ")");
                }
            }
            
            // Check attendance
            List<AttendanceRecord> attendanceRecords = attendanceRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!attendanceRecords.isEmpty()) {
                long presentCount = attendanceRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendancePercentage = (double) presentCount / attendanceRecords.size() * 100;
                
                if (attendancePercentage < 75.0) {
                    needsAttention = true;
                    reasons.add("Low attendance (" + Math.round(attendancePercentage * 100.0) / 100.0 + "%)");
                }
            }
            
            if (needsAttention) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentId", student.getId());
                studentInfo.put("studentName", student.getUser().getName());
                studentInfo.put("studentNumber", student.getStudentId());
                studentInfo.put("reasons", reasons);
                needingAttention.add(studentInfo);
            }
        }
        
        return needingAttention;
    }
}