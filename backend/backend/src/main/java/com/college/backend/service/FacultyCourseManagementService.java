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
import java.util.*;

@Service
@Transactional
public class FacultyCourseManagementService {

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

    // Course Overview and Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getFacultyCourseOverview() {
        Faculty faculty = getCurrentFaculty();
        Map<String, Object> overview = new HashMap<>();
        
        // Get all courses assigned to this faculty
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        overview.put("totalAssignedCourses", assignedCourses.size());
        
        // Course status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Course.CourseStatus status : Course.CourseStatus.values()) {
            long count = assignedCourses.stream()
                    .filter(course -> course.getStatus() == status)
                    .count();
            statusDistribution.put(status.name(), count);
        }
        overview.put("courseStatusDistribution", statusDistribution);
        
        // Course type distribution
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Course.CourseType type : Course.CourseType.values()) {
            long count = assignedCourses.stream()
                    .filter(course -> course.getType() == type)
                    .count();
            typeDistribution.put(type.name(), count);
        }
        overview.put("courseTypeDistribution", typeDistribution);
        
        // Total enrolled students across all courses
        int totalEnrolledStudents = assignedCourses.stream()
                .mapToInt(course -> (int) enrollmentRepository.countByCourseId(course.getId()))
                .sum();
        overview.put("totalEnrolledStudents", totalEnrolledStudents);
        
        // Course details with enrollment data
        List<Map<String, Object>> courseDetails = new ArrayList<>();
        for (Course course : assignedCourses) {
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("courseId", course.getId());
            courseInfo.put("courseName", course.getCourseName());
            courseInfo.put("courseCode", course.getCourseCode());
            courseInfo.put("credits", course.getCredits());
            courseInfo.put("status", course.getStatus());
            courseInfo.put("type", course.getType());
            courseInfo.put("enrolledCount", enrollmentRepository.countByCourseId(course.getId()));
            courseInfo.put("maxCapacity", course.getMaxCapacity());
            
            // Calculate enrollment percentage
            if (course.getMaxCapacity() != null && course.getMaxCapacity() > 0) {
                double enrollmentPercentage = ((double) enrollmentRepository.countByCourseId(course.getId()) / course.getMaxCapacity()) * 100;
                courseInfo.put("enrollmentPercentage", Math.round(enrollmentPercentage * 100.0) / 100.0);
            } else {
                courseInfo.put("enrollmentPercentage", 0.0);
            }
            
            courseDetails.add(courseInfo);
        }
        overview.put("courseDetails", courseDetails);
        
        return overview;
    }

    // Course Material Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseDetails(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> details = new HashMap<>();
        details.put("course", course);
        
        // Enrollment information
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        details.put("enrollments", enrollments);
        details.put("enrolledStudentCount", enrollments.size());
        
        // Student details
        List<Map<String, Object>> studentDetails = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Map<String, Object> studentInfo = new HashMap<>();
            Student student = enrollment.getStudent();
            studentInfo.put("studentId", student.getId());
            studentInfo.put("studentNumber", student.getStudentId());
            studentInfo.put("name", student.getUser().getName());
            studentInfo.put("email", student.getUser().getEmail());
            studentInfo.put("enrollmentDate", enrollment.getEnrollmentDate());
            studentInfo.put("status", enrollment.getStatus());
            studentInfo.put("currentGrade", enrollment.getCurrentGrade());
            
            studentDetails.add(studentInfo);
        }
        details.put("enrolledStudents", studentDetails);
        
        // Recent activity in the course
        List<Map<String, Object>> recentActivity = getRecentCourseActivity(courseId);
        details.put("recentActivity", recentActivity);
        
        return details;
    }

    @PreAuthorize("hasRole('FACULTY')")
    public Course updateCourseContent(Long courseId, Map<String, Object> courseUpdates) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        // Update allowed fields for faculty
        if (courseUpdates.containsKey("description")) {
            course.setDescription((String) courseUpdates.get("description"));
        }
        if (courseUpdates.containsKey("syllabus")) {
            course.setSyllabus((String) courseUpdates.get("syllabus"));
        }
        if (courseUpdates.containsKey("objectives")) {
            course.setObjectives((String) courseUpdates.get("objectives"));
        }
        if (courseUpdates.containsKey("schedule")) {
            course.setSchedule((String) courseUpdates.get("schedule"));
        }
        if (courseUpdates.containsKey("classroom")) {
            course.setClassroom((String) courseUpdates.get("classroom"));
        }
        if (courseUpdates.containsKey("maxCapacity") && faculty.getDesignation().ordinal() >= Faculty.Designation.ASSOCIATE_PROFESSOR.ordinal()) {
            // Only senior faculty can update capacity
            course.setMaxCapacity((Integer) courseUpdates.get("maxCapacity"));
        }
        
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // Course Analytics for Faculty
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseAnalytics(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Enrollment analytics
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        analytics.put("totalEnrollments", enrollments.size());
        
        // Enrollment status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Enrollment.EnrollmentStatus status : Enrollment.EnrollmentStatus.values()) {
            long count = enrollments.stream()
                    .filter(enrollment -> enrollment.getStatus() == status)
                    .count();
            statusDistribution.put(status.name(), count);
        }
        analytics.put("enrollmentStatusDistribution", statusDistribution);
        
        // Grade distribution
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        Map<String, Object> gradeAnalytics = analyzeGrades(grades);
        analytics.put("gradeAnalytics", gradeAnalytics);
        
        // Attendance analytics
        List<AttendanceRecord> attendanceRecords = attendanceRepository.findByCourseId(courseId);
        Map<String, Object> attendanceAnalytics = analyzeAttendance(attendanceRecords, enrollments.size());
        analytics.put("attendanceAnalytics", attendanceAnalytics);
        
        // Student performance insights
        List<Map<String, Object>> studentPerformance = analyzeStudentPerformance(courseId);
        analytics.put("studentPerformanceInsights", studentPerformance);
        
        return analytics;
    }

    // Course Schedule Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> updateCourseSchedule(Long courseId, Map<String, Object> scheduleData) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        // Update schedule information
        if (scheduleData.containsKey("schedule")) {
            course.setSchedule((String) scheduleData.get("schedule"));
        }
        if (scheduleData.containsKey("classroom")) {
            course.setClassroom((String) scheduleData.get("classroom"));
        }
        
        course.setUpdatedAt(LocalDateTime.now());
        Course updatedCourse = courseRepository.save(course);
        
        // Log the schedule change
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Course schedule updated successfully");
        result.put("course", updatedCourse);
        result.put("updatedBy", faculty.getUser().getName());
        result.put("updateTime", LocalDateTime.now());
        
        return result;
    }

    // Course Status Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> updateCourseStatus(Long courseId, Course.CourseStatus newStatus) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        // Validate status transitions
        Course.CourseStatus currentStatus = course.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus, faculty)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        course.setStatus(newStatus);
        course.setUpdatedAt(LocalDateTime.now());
        Course updatedCourse = courseRepository.save(course);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Course status updated from " + currentStatus + " to " + newStatus);
        result.put("course", updatedCourse);
        result.put("previousStatus", currentStatus);
        result.put("newStatus", newStatus);
        result.put("updatedBy", faculty.getUser().getName());
        result.put("updateTime", LocalDateTime.now());
        
        return result;
    }

    // Course Capacity Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> manageCourseCapacity(Long courseId, Integer newCapacity) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        // Check if faculty has permission to change capacity
        if (faculty.getDesignation().ordinal() < Faculty.Designation.ASSOCIATE_PROFESSOR.ordinal()) {
            throw new IllegalArgumentException("Insufficient privileges to modify course capacity");
        }
        
        Integer currentCapacity = course.getMaxCapacity();
        Long currentEnrollments = enrollmentRepository.countByCourseId(courseId);
        
        // Validate new capacity
        if (newCapacity < currentEnrollments) {
            throw new IllegalArgumentException("New capacity (" + newCapacity + ") cannot be less than current enrollments (" + currentEnrollments + ")");
        }
        
        course.setMaxCapacity(newCapacity);
        course.setUpdatedAt(LocalDateTime.now());
        Course updatedCourse = courseRepository.save(course);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Course capacity updated successfully");
        result.put("course", updatedCourse);
        result.put("previousCapacity", currentCapacity);
        result.put("newCapacity", newCapacity);
        result.put("currentEnrollments", currentEnrollments);
        result.put("availableSlots", newCapacity - currentEnrollments.intValue());
        result.put("updatedBy", faculty.getUser().getName());
        
        return result;
    }

    // Course Performance Summary
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCoursePerformanceSummary(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> summary = new HashMap<>();
        
        // Basic course info
        summary.put("courseId", course.getId());
        summary.put("courseName", course.getCourseName());
        summary.put("courseCode", course.getCourseCode());
        
        // Enrollment summary
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        summary.put("totalEnrolled", enrollments.size());
        summary.put("activeEnrollments", enrollments.stream().filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED).count());
        
        // Grade summary
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        if (!grades.isEmpty()) {
            double averageGrade = grades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            summary.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
            summary.put("totalGradesEntered", grades.size());
            summary.put("passRate", calculatePassRate(grades));
        } else {
            summary.put("averageGrade", 0.0);
            summary.put("totalGradesEntered", 0);
            summary.put("passRate", 0.0);
        }
        
        // Attendance summary
        List<AttendanceRecord> attendanceRecords = attendanceRepository.findByCourseId(courseId);
        if (!attendanceRecords.isEmpty()) {
            double averageAttendance = attendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count() * 100.0 / attendanceRecords.size();
            summary.put("averageAttendance", Math.round(averageAttendance * 100.0) / 100.0);
        } else {
            summary.put("averageAttendance", 0.0);
        }
        
        return summary;
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
        
        // Check if faculty is assigned to this course
        if (!course.getInstructor().getId().equals(faculty.getId())) {
            throw new IllegalArgumentException("Access denied: You are not assigned to this course");
        }
        
        return course;
    }

    private List<Map<String, Object>> getRecentCourseActivity(Long courseId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent enrollments
        List<Enrollment> recentEnrollments = enrollmentRepository.findByCourseIdOrderByEnrollmentDateDesc(courseId)
                .stream().limit(5).toList();
        for (Enrollment enrollment : recentEnrollments) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "ENROLLMENT");
            activity.put("description", enrollment.getStudent().getUser().getName() + " enrolled in the course");
            activity.put("timestamp", enrollment.getEnrollmentDate());
            activities.add(activity);
        }
        
        // Recent grades
        List<Grade> recentGrades = gradeRepository.findByCourseIdOrderByGradedDateDesc(courseId)
                .stream().limit(5).toList();
        for (Grade grade : recentGrades) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "GRADE");
            activity.put("description", "Grade entered for " + grade.getStudent().getUser().getName() + ": " + grade.getLetterGrade());
            activity.put("timestamp", grade.getGradeDate());
            activities.add(activity);
        }
        
        // Sort by timestamp
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return activities.stream().limit(10).toList();
    }

    private Map<String, Object> analyzeGrades(List<Grade> grades) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (grades.isEmpty()) {
            analysis.put("totalGrades", 0);
            analysis.put("averageGrade", 0.0);
            analysis.put("gradeDistribution", new HashMap<>());
            return analysis;
        }
        
        analysis.put("totalGrades", grades.size());
        
        // Calculate average numeric grade
        double average = grades.stream()
                .filter(g -> g.getNumericGrade() != null)
                .mapToDouble(Grade::getNumericGrade)
                .average()
                .orElse(0.0);
        analysis.put("averageGrade", Math.round(average * 100.0) / 100.0);
        
        // Grade distribution
        Map<String, Long> distribution = new HashMap<>();
        for (Grade grade : grades) {
            if (grade.getLetterGrade() != null) {
                distribution.merge(grade.getLetterGrade(), 1L, Long::sum);
            }
        }
        analysis.put("gradeDistribution", distribution);
        
        // Pass rate
        analysis.put("passRate", calculatePassRate(grades));
        
        return analysis;
    }

    private Map<String, Object> analyzeAttendance(List<AttendanceRecord> records, int totalStudents) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (records.isEmpty()) {
            analysis.put("totalRecords", 0);
            analysis.put("averageAttendance", 0.0);
            analysis.put("attendanceRate", 0.0);
            return analysis;
        }
        
        analysis.put("totalRecords", records.size());
        
        long presentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();
        
        double attendanceRate = (double) presentCount / records.size() * 100;
        analysis.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        
        // Average attendance per session
        if (totalStudents > 0) {
            double avgAttendancePerSession = (double) presentCount / totalStudents;
            analysis.put("averageAttendancePerSession", Math.round(avgAttendancePerSession * 100.0) / 100.0);
        } else {
            analysis.put("averageAttendancePerSession", 0.0);
        }
        
        return analysis;
    }

    private List<Map<String, Object>> analyzeStudentPerformance(Long courseId) {
        List<Map<String, Object>> performance = new ArrayList<>();
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        for (Enrollment enrollment : enrollments) {
            Map<String, Object> studentPerf = new HashMap<>();
            Student student = enrollment.getStudent();
            
            studentPerf.put("studentId", student.getId());
            studentPerf.put("studentName", student.getUser().getName());
            studentPerf.put("enrollmentStatus", enrollment.getStatus());
            
            // Get latest grade
            Optional<Grade> latestGrade = gradeRepository.findByStudentIdAndCourseIdOrderByGradedDateDesc(student.getId(), courseId)
                    .stream().findFirst();
            if (latestGrade.isPresent()) {
                studentPerf.put("currentGrade", latestGrade.get().getLetterGrade());
                studentPerf.put("numericGrade", latestGrade.get().getNumericGrade());
            } else {
                studentPerf.put("currentGrade", "N/A");
                studentPerf.put("numericGrade", null);
            }
            
            // Calculate attendance percentage for this student
            List<AttendanceRecord> studentAttendance = attendanceRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (!studentAttendance.isEmpty()) {
                long presentCount = studentAttendance.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                double attendancePercentage = (double) presentCount / studentAttendance.size() * 100;
                studentPerf.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
            } else {
                studentPerf.put("attendancePercentage", 0.0);
            }
            
            performance.add(studentPerf);
        }
        
        return performance;
    }

    private boolean isValidStatusTransition(Course.CourseStatus current, Course.CourseStatus target, Faculty faculty) {
        // Define valid transitions based on faculty role
        switch (current) {
            case DRAFT:
                return target == Course.CourseStatus.ACTIVE || target == Course.CourseStatus.INACTIVE;
            case ACTIVE:
                return target == Course.CourseStatus.INACTIVE || target == Course.CourseStatus.COMPLETED;
            case INACTIVE:
                return target == Course.CourseStatus.ACTIVE || (faculty.getDesignation().ordinal() >= Faculty.Designation.PROFESSOR.ordinal() && target == Course.CourseStatus.DRAFT);
            case COMPLETED:
                return faculty.getDesignation().ordinal() >= Faculty.Designation.PROFESSOR.ordinal() && (target == Course.CourseStatus.ACTIVE || target == Course.CourseStatus.INACTIVE);
            default:
                return false;
        }
    }

    private double calculatePassRate(List<Grade> grades) {
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        long passingGrades = grades.stream()
                .filter(g -> g.getNumericGrade() != null && g.getNumericGrade() >= 60.0) // Assuming 60 is passing
                .count();
        
        return (double) passingGrades / grades.size() * 100;
    }
}