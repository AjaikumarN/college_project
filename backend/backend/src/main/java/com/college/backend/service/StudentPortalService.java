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
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentPortalService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    // Student Dashboard Overview
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> getStudentDashboardOverview() {
        Student student = getCurrentStudent();
        Map<String, Object> dashboard = new HashMap<>();
        
        // Student Profile Information
        Map<String, Object> studentProfile = new HashMap<>();
        studentProfile.put("id", student.getId());
        studentProfile.put("name", student.getUser().getName());
        studentProfile.put("email", student.getUser().getEmail());
        studentProfile.put("phone", student.getUser().getPhone());
        studentProfile.put("studentId", student.getStudentId());
        studentProfile.put("status", student.getStatus());
        studentProfile.put("feeStatus", student.getFeeStatus());
        studentProfile.put("cgpa", student.getCgpa());
        studentProfile.put("academicYear", student.getAcademicYear());
        studentProfile.put("semester", student.getSemester());
        studentProfile.put("hostelResident", student.getHostelResident());
        
        if (student.getDepartment() != null) {
            studentProfile.put("department", Map.of(
                "id", student.getDepartment().getId(),
                "name", student.getDepartment().getName(),
                "code", student.getDepartment().getCode()
            ));
        }
        
        dashboard.put("studentProfile", studentProfile);
        
        // Academic Overview
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        Map<String, Object> academicOverview = new HashMap<>();
        academicOverview.put("totalEnrolledCourses", enrollments.size());
        
        // Current semester enrollments
        List<Enrollment> currentEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .collect(Collectors.toList());
        academicOverview.put("currentEnrolledCourses", currentEnrollments.size());
        
        // Calculate total credits
        int totalCredits = currentEnrollments.stream()
                .mapToInt(e -> e.getCourse().getCredits() != null ? e.getCourse().getCredits() : 0)
                .sum();
        academicOverview.put("totalCredits", totalCredits);
        
        // Enrollment status distribution
        Map<String, Long> enrollmentStatusDistribution = enrollments.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                ));
        academicOverview.put("enrollmentStatusDistribution", enrollmentStatusDistribution);
        
        dashboard.put("academicOverview", academicOverview);
        
        // Performance Overview
        Map<String, Object> performanceOverview = getPerformanceOverview(student);
        dashboard.put("performanceOverview", performanceOverview);
        
        // Attendance Overview
        Map<String, Object> attendanceOverview = getAttendanceOverview(student);
        dashboard.put("attendanceOverview", attendanceOverview);
        
        // Recent Activities
        List<Map<String, Object>> recentActivities = getRecentActivities(student, 10);
        dashboard.put("recentActivities", recentActivities);
        
        // Upcoming Events/Deadlines
        List<Map<String, Object>> upcomingEvents = getUpcomingEvents(student);
        dashboard.put("upcomingEvents", upcomingEvents);
        
        return dashboard;
    }

    // Course Enrollment Management
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> getAvailableCoursesForEnrollment() {
        Student student = getCurrentStudent();
        Map<String, Object> result = new HashMap<>();
        
        // Get all active courses
        List<Course> allCourses = courseRepository.findByStatus(Course.CourseStatus.ACTIVE);
        
        // Get currently enrolled course IDs
        List<Enrollment> currentEnrollments = enrollmentRepository.findByStudentId(student.getId());
        Set<Long> enrolledCourseIds = currentEnrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
        
        // Filter available courses
        List<Course> availableCourses = allCourses.stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .filter(course -> course.getMaxCapacity() == null || 
                    enrollmentRepository.countByCourseId(course.getId()) < course.getMaxCapacity())
                .collect(Collectors.toList());
        
        List<Map<String, Object>> courseDetails = new ArrayList<>();
        for (Course course : availableCourses) {
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("id", course.getId());
            courseInfo.put("courseName", course.getCourseName());
            courseInfo.put("courseCode", course.getCourseCode());
            courseInfo.put("credits", course.getCredits());
            courseInfo.put("type", course.getType());
            courseInfo.put("description", course.getDescription());
            courseInfo.put("schedule", course.getSchedule());
            courseInfo.put("classroom", course.getClassroom());
            courseInfo.put("maxCapacity", course.getMaxCapacity());
            courseInfo.put("currentEnrollment", enrollmentRepository.countByCourseId(course.getId()));
            courseInfo.put("prerequisites", course.getPrerequisites());
            
            if (course.getInstructor() != null) {
                courseInfo.put("instructor", Map.of(
                    "name", course.getInstructor().getUser().getName(),
                    "designation", course.getInstructor().getDesignation()
                ));
            }
            
            if (course.getDepartment() != null) {
                courseInfo.put("department", Map.of(
                    "name", course.getDepartment().getName(),
                    "code", course.getDepartment().getCode()
                ));
            }
            
            courseDetails.add(courseInfo);
        }
        
        result.put("availableCourses", courseDetails);
        result.put("totalAvailable", courseDetails.size());
        
        return result;
    }

    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> enrollInCourse(Long courseId) {
        Student student = getCurrentStudent();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
            // Check if already enrolled
            Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            if (existingEnrollment.isPresent()) {
                throw new IllegalArgumentException("Already enrolled in this course");
            }
            
            // Check capacity
            Long currentEnrollment = enrollmentRepository.countByCourseId(courseId);
            if (course.getMaxCapacity() != null && currentEnrollment >= course.getMaxCapacity()) {
                throw new IllegalArgumentException("Course is at maximum capacity");
            }
            
            // Create enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
            
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            
            result.put("success", true);
            result.put("message", "Successfully enrolled in " + course.getCourseName());
            result.put("enrollment", Map.of(
                "enrollmentId", savedEnrollment.getId(),
                "courseName", course.getCourseName(),
                "courseCode", course.getCourseCode(),
                "credits", course.getCredits(),
                "enrollmentDate", savedEnrollment.getEnrollmentDate()
            ));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Enrollment failed: " + e.getMessage());
        }
        
        return result;
    }

    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> dropCourse(Long courseId) {
        Student student = getCurrentStudent();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
            
            // Check if dropping is allowed (you can add business rules here)
            if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
                throw new IllegalArgumentException("Cannot drop a completed course");
            }
            
            enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
            enrollment.setUpdatedAt(LocalDateTime.now());
            enrollmentRepository.save(enrollment);
            
            result.put("success", true);
            result.put("message", "Successfully dropped from " + enrollment.getCourse().getCourseName());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Drop failed: " + e.getMessage());
        }
        
        return result;
    }

    // Academic Records
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> getAcademicRecords() {
        Student student = getCurrentStudent();
        Map<String, Object> records = new HashMap<>();
        
        // Get all enrollments
        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(student.getId());
        
        List<Map<String, Object>> courseRecords = new ArrayList<>();
        for (Enrollment enrollment : allEnrollments) {
            Map<String, Object> record = new HashMap<>();
            Course course = enrollment.getCourse();
            
            record.put("enrollmentId", enrollment.getId());
            record.put("courseName", course.getCourseName());
            record.put("courseCode", course.getCourseCode());
            record.put("credits", course.getCredits());
            record.put("type", course.getType());
            record.put("enrollmentDate", enrollment.getEnrollmentDate());
            record.put("enrollmentStatus", enrollment.getStatus());
            record.put("currentGrade", enrollment.getCurrentGrade());
            
            if (course.getInstructor() != null) {
                record.put("instructor", course.getInstructor().getUser().getName());
            }
            
            // Get grades for this course
            List<Grade> courseGrades = gradeRepository.findByStudentIdAndCourseIdOrderByGradedDateDesc(student.getId(), course.getId());
            List<Map<String, Object>> gradeDetails = new ArrayList<>();
            
            for (Grade grade : courseGrades) {
                Map<String, Object> gradeInfo = new HashMap<>();
                gradeInfo.put("gradeType", grade.getGradeType());
                gradeInfo.put("letterGrade", grade.getLetterGrade());
                gradeInfo.put("numericGrade", grade.getNumericGrade());
                gradeInfo.put("maxPoints", grade.getMaxPoints());
                gradeInfo.put("pointsEarned", grade.getPointsEarned());
                gradeInfo.put("gradeDate", grade.getGradeDate());
                gradeInfo.put("comments", grade.getComments());
                gradeDetails.add(gradeInfo);
            }
            record.put("grades", gradeDetails);
            
            // Calculate course average
            if (!courseGrades.isEmpty()) {
                double averageGrade = courseGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                record.put("courseAverage", Math.round(averageGrade * 100.0) / 100.0);
            } else {
                record.put("courseAverage", null);
            }
            
            courseRecords.add(record);
        }
        
        records.put("courseRecords", courseRecords);
        records.put("totalCourses", courseRecords.size());
        
        // Academic summary
        Map<String, Object> academicSummary = new HashMap<>();
        academicSummary.put("overallCGPA", student.getCgpa());
        academicSummary.put("currentSemester", student.getSemester());
        academicSummary.put("academicYear", student.getAcademicYear());
        academicSummary.put("status", student.getStatus());
        
        // Calculate completed credits
        int completedCredits = allEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .mapToInt(e -> e.getCourse().getCredits() != null ? e.getCourse().getCredits() : 0)
                .sum();
        academicSummary.put("completedCredits", completedCredits);
        
        records.put("academicSummary", academicSummary);
        
        return records;
    }

    // Attendance Tracking
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> getAttendanceRecords() {
        Student student = getCurrentStudent();
        Map<String, Object> attendanceData = new HashMap<>();
        
        // Get all attendance records
        List<AttendanceRecord> allAttendance = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(student.getId());
        
        // Group by course
        Map<Long, List<AttendanceRecord>> attendanceByCourse = allAttendance.stream()
                .collect(Collectors.groupingBy(a -> a.getCourse().getId()));
        
        List<Map<String, Object>> courseAttendance = new ArrayList<>();
        
        for (Map.Entry<Long, List<AttendanceRecord>> entry : attendanceByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<AttendanceRecord> courseRecords = entry.getValue();
            
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("courseId", course.getId());
                courseData.put("courseName", course.getCourseName());
                courseData.put("courseCode", course.getCourseCode());
                
                // Calculate attendance statistics
                long totalClasses = courseRecords.size();
                long presentCount = courseRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                        .count();
                long absentCount = courseRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                        .count();
                long lateCount = courseRecords.stream()
                        .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.LATE)
                        .count();
                
                double attendancePercentage = totalClasses > 0 ? (double) presentCount / totalClasses * 100 : 0.0;
                
                courseData.put("totalClasses", totalClasses);
                courseData.put("present", presentCount);
                courseData.put("absent", absentCount);
                courseData.put("late", lateCount);
                courseData.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
                
                // Recent attendance records (last 10)
                List<Map<String, Object>> recentRecords = courseRecords.stream()
                        .limit(10)
                        .map(record -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("date", record.getAttendanceDate());
                            map.put("status", record.getStatus());
                            map.put("remarks", record.getRemarks() != null ? record.getRemarks() : "");
                            return map;
                        })
                        .collect(Collectors.toList());
                courseData.put("recentRecords", recentRecords);
                
                courseAttendance.add(courseData);
            }
        }
        
        attendanceData.put("courseAttendance", courseAttendance);
        
        // Overall attendance summary
        long totalClasses = allAttendance.size();
        long totalPresent = allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();
        double overallAttendancePercentage = totalClasses > 0 ? (double) totalPresent / totalClasses * 100 : 0.0;
        
        Map<String, Object> overallSummary = new HashMap<>();
        overallSummary.put("totalClasses", totalClasses);
        overallSummary.put("totalPresent", totalPresent);
        overallSummary.put("overallAttendancePercentage", Math.round(overallAttendancePercentage * 100.0) / 100.0);
        
        attendanceData.put("overallSummary", overallSummary);
        
        return attendanceData;
    }

    // Grade Viewing
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, Object> getGradeDetails() {
        Student student = getCurrentStudent();
        Map<String, Object> gradeData = new HashMap<>();
        
        // Get all grades
        List<Grade> allGrades = gradeRepository.findByStudentIdOrderByGradedDateDesc(student.getId());
        
        // Group by course
        Map<Long, List<Grade>> gradesByCourse = allGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getCourse().getId()));
        
        List<Map<String, Object>> courseGrades = new ArrayList<>();
        
        for (Map.Entry<Long, List<Grade>> entry : gradesByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<Grade> courseGradeList = entry.getValue();
            
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("courseId", course.getId());
                courseData.put("courseName", course.getCourseName());
                courseData.put("courseCode", course.getCourseCode());
                courseData.put("credits", course.getCredits());
                
                // Grade details
                List<Map<String, Object>> gradeDetails = new ArrayList<>();
                for (Grade grade : courseGradeList) {
                    Map<String, Object> gradeInfo = new HashMap<>();
                    gradeInfo.put("gradeId", grade.getId());
                    gradeInfo.put("gradeType", grade.getGradeType());
                    gradeInfo.put("letterGrade", grade.getLetterGrade());
                    gradeInfo.put("numericGrade", grade.getNumericGrade());
                    gradeInfo.put("maxPoints", grade.getMaxPoints());
                    gradeInfo.put("pointsEarned", grade.getPointsEarned());
                    gradeInfo.put("gradeDate", grade.getGradeDate());
                    gradeInfo.put("comments", grade.getComments());
                    gradeDetails.add(gradeInfo);
                }
                courseData.put("grades", gradeDetails);
                
                // Calculate course statistics
                if (!courseGradeList.isEmpty()) {
                    double averageGrade = courseGradeList.stream()
                            .filter(g -> g.getNumericGrade() != null)
                            .mapToDouble(Grade::getNumericGrade)
                            .average()
                            .orElse(0.0);
                    courseData.put("courseAverage", Math.round(averageGrade * 100.0) / 100.0);
                    
                    // Find latest grade
                    Grade latestGrade = courseGradeList.stream()
                            .max(Comparator.comparing(Grade::getGradeDate))
                            .orElse(null);
                    if (latestGrade != null) {
                        courseData.put("currentGrade", latestGrade.getLetterGrade());
                    }
                } else {
                    courseData.put("courseAverage", null);
                    courseData.put("currentGrade", null);
                }
                
                courseGrades.add(courseData);
            }
        }
        
        gradeData.put("courseGrades", courseGrades);
        
        // Overall grade summary
        Map<String, Object> overallSummary = new HashMap<>();
        overallSummary.put("totalGrades", allGrades.size());
        overallSummary.put("cgpa", student.getCgpa());
        
        if (!allGrades.isEmpty()) {
            double overallAverage = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            overallSummary.put("overallAverage", Math.round(overallAverage * 100.0) / 100.0);
            
            // Grade distribution
            Map<String, Long> gradeDistribution = allGrades.stream()
                    .filter(g -> g.getLetterGrade() != null)
                    .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
            overallSummary.put("gradeDistribution", gradeDistribution);
        } else {
            overallSummary.put("overallAverage", 0.0);
            overallSummary.put("gradeDistribution", new HashMap<>());
        }
        
        gradeData.put("overallSummary", overallSummary);
        
        return gradeData;
    }

    // Helper Methods
    private Student getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private Map<String, Object> getPerformanceOverview(Student student) {
        Map<String, Object> performance = new HashMap<>();
        
        List<Grade> allGrades = gradeRepository.findByStudentId(student.getId());
        performance.put("totalGrades", allGrades.size());
        performance.put("cgpa", student.getCgpa());
        
        if (!allGrades.isEmpty()) {
            double averageGrade = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            performance.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
            
            // Performance trend (simplified)
            Grade latestGrade = allGrades.stream()
                    .max(Comparator.comparing(Grade::getGradeDate))
                    .orElse(null);
            if (latestGrade != null) {
                performance.put("latestGrade", latestGrade.getLetterGrade());
                performance.put("latestNumericGrade", latestGrade.getNumericGrade());
            }
        } else {
            performance.put("averageGrade", 0.0);
            performance.put("latestGrade", null);
            performance.put("latestNumericGrade", null);
        }
        
        return performance;
    }

    private Map<String, Object> getAttendanceOverview(Student student) {
        Map<String, Object> attendance = new HashMap<>();
        
        List<AttendanceRecord> allAttendance = attendanceRepository.findByStudentId(student.getId());
        attendance.put("totalRecords", allAttendance.size());
        
        if (!allAttendance.isEmpty()) {
            long presentCount = allAttendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            double attendancePercentage = (double) presentCount / allAttendance.size() * 100;
            attendance.put("overallAttendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
            attendance.put("totalPresent", presentCount);
            attendance.put("totalAbsent", allAttendance.stream()
                    .filter(a -> a.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                    .count());
        } else {
            attendance.put("overallAttendancePercentage", 0.0);
            attendance.put("totalPresent", 0);
            attendance.put("totalAbsent", 0);
        }
        
        return attendance;
    }

    private List<Map<String, Object>> getRecentActivities(Student student, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent enrollments
        List<Enrollment> recentEnrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(student.getId())
                .stream().limit(limit / 2).collect(Collectors.toList());
        
        for (Enrollment enrollment : recentEnrollments) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "ENROLLMENT");
            activity.put("description", "Enrolled in " + enrollment.getCourse().getCourseName());
            activity.put("timestamp", enrollment.getEnrollmentDate());
            activity.put("courseName", enrollment.getCourse().getCourseName());
            activities.add(activity);
        }
        
        // Recent grades
        List<Grade> recentGrades = gradeRepository.findByStudentIdOrderByGradedDateDesc(student.getId())
                .stream().limit(limit / 2).collect(Collectors.toList());
        
        for (Grade grade : recentGrades) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "GRADE_RECEIVED");
            activity.put("description", "Received grade " + grade.getLetterGrade() + 
                       " in " + grade.getCourse().getCourseName());
            activity.put("timestamp", grade.getGradeDate());
            activity.put("courseName", grade.getCourse().getCourseName());
            activity.put("grade", grade.getLetterGrade());
            activities.add(activity);
        }
        
        // Sort by timestamp and limit
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return activities.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getUpcomingEvents(Student student) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        // This would typically involve checking for:
        // - Assignment deadlines
        // - Exam schedules
        // - Fee payment deadlines
        // - Course registration periods
        
        // For now, returning sample events
        Map<String, Object> feeReminder = new HashMap<>();
        feeReminder.put("type", "FEE_PAYMENT");
        feeReminder.put("title", "Fee Payment Due");
        feeReminder.put("description", "Semester fee payment deadline approaching");
        feeReminder.put("dueDate", LocalDateTime.now().plusDays(15));
        feeReminder.put("priority", "HIGH");
        events.add(feeReminder);
        
        if (student.getFeeStatus() == Student.FeeStatus.PENDING) {
            Map<String, Object> feeOverdue = new HashMap<>();
            feeOverdue.put("type", "FEE_OVERDUE");
            feeOverdue.put("title", "Fee Payment Overdue");
            feeOverdue.put("description", "Please complete your fee payment immediately");
            feeOverdue.put("dueDate", LocalDateTime.now().minusDays(5));
            feeOverdue.put("priority", "CRITICAL");
            events.add(feeOverdue);
        }
        
        return events;
    }
}