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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyGradeManagementService {

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

    // Grade Management Overview
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getGradeManagementOverview() {
        Faculty faculty = getCurrentFaculty();
        Map<String, Object> overview = new HashMap<>();
        
        // Get all courses taught by this faculty
        List<Course> assignedCourses = courseRepository.findByInstructorId(faculty.getId());
        overview.put("totalCoursesTeaching", assignedCourses.size());
        
        // Total grades entered across all courses
        int totalGradesEntered = 0;
        int totalStudentsToGrade = 0;
        
        for (Course course : assignedCourses) {
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            totalGradesEntered += courseGrades.size();
            
            int enrolledCount = (int) enrollmentRepository.countByCourseId(course.getId());
            totalStudentsToGrade += enrolledCount;
        }
        
        overview.put("totalGradesEntered", totalGradesEntered);
        overview.put("totalStudentsToGrade", totalStudentsToGrade);
        overview.put("gradingProgress", totalStudentsToGrade > 0 ? 
            Math.round((double) totalGradesEntered / totalStudentsToGrade * 100.0) / 100.0 : 0.0);
        
        // Grade distribution overview
        Map<String, Object> gradeDistribution = new HashMap<>();
        List<Grade> allGrades = new ArrayList<>();
        for (Course course : assignedCourses) {
            allGrades.addAll(gradeRepository.findByCourseId(course.getId()));
        }
        
        Map<String, Long> letterGradeDistribution = allGrades.stream()
                .filter(g -> g.getLetterGrade() != null)
                .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
        gradeDistribution.put("letterGradeDistribution", letterGradeDistribution);
        
        // Average grades across all courses
        if (!allGrades.isEmpty()) {
            double averageGrade = allGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .mapToDouble(Grade::getNumericGrade)
                    .average()
                    .orElse(0.0);
            gradeDistribution.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);
        } else {
            gradeDistribution.put("averageGrade", 0.0);
        }
        
        overview.put("gradeDistribution", gradeDistribution);
        
        // Recent grading activity
        List<Map<String, Object>> recentGrades = getRecentGradingActivity(assignedCourses, 10);
        overview.put("recentGradingActivity", recentGrades);
        
        // Pending grading tasks
        List<Map<String, Object>> pendingTasks = getPendingGradingTasks(assignedCourses);
        overview.put("pendingGradingTasks", pendingTasks);
        
        return overview;
    }

    // Course-specific Grade Management
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseGradeOverview(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode(),
            "credits", course.getCredits()
        ));
        
        // Get all enrollments for the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        overview.put("totalEnrolledStudents", enrollments.size());
        
        // Get all grades for the course
        List<Grade> courseGrades = gradeRepository.findByCourseId(courseId);
        overview.put("totalGradesEntered", courseGrades.size());
        
        // Calculate grading progress
        Map<Long, Integer> studentGradeCount = courseGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getStudent().getId(), 
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
        
        int studentsWithGrades = studentGradeCount.size();
        overview.put("studentsGraded", studentsWithGrades);
        overview.put("studentsRemaining", enrollments.size() - studentsWithGrades);
        overview.put("gradingCompletionPercentage", enrollments.size() > 0 ? 
            Math.round((double) studentsWithGrades / enrollments.size() * 100.0) / 100.0 : 0.0);
        
        // Grade statistics
        if (!courseGrades.isEmpty()) {
            List<Double> numericGrades = courseGrades.stream()
                    .filter(g -> g.getNumericGrade() != null)
                    .map(Grade::getNumericGrade)
                    .collect(Collectors.toList());
            
            if (!numericGrades.isEmpty()) {
                double averageGrade = numericGrades.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double maxGrade = numericGrades.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double minGrade = numericGrades.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
                overview.put("gradeStatistics", Map.of(
                    "average", Math.round(averageGrade * 100.0) / 100.0,
                    "highest", maxGrade,
                    "lowest", minGrade
                ));
            }
        }
        
        // Grade distribution by letter grade
        Map<String, Long> letterGradeDistribution = courseGrades.stream()
                .filter(g -> g.getLetterGrade() != null)
                .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
        overview.put("letterGradeDistribution", letterGradeDistribution);
        
        // Grade type distribution
        Map<String, Long> gradeTypeDistribution = courseGrades.stream()
                .filter(g -> g.getGradeType() != null)
                .collect(Collectors.groupingBy(Grade::getGradeType, Collectors.counting()));
        overview.put("gradeTypeDistribution", gradeTypeDistribution);
        
        return overview;
    }

    // Enter/Update Grades
    @PreAuthorize("hasRole('FACULTY')")
    public Grade enterGrade(Long courseId, Long studentId, Grade gradeData) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Verify student is enrolled in the course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in this course"));
        
        // Create new grade
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setGradeType(gradeData.getGradeType());
        grade.setLetterGrade(gradeData.getLetterGrade());
        grade.setNumericGrade(gradeData.getNumericGrade());
        grade.setMaxPoints(gradeData.getMaxPoints());
        grade.setPointsEarned(gradeData.getPointsEarned());
        grade.setComments(gradeData.getComments());
        grade.setGradeDate(LocalDateTime.now());
        grade.setCreatedAt(LocalDateTime.now());
        
        // Auto-calculate letter grade if not provided
        if (grade.getLetterGrade() == null && grade.getNumericGrade() != null) {
            grade.setLetterGrade(calculateLetterGrade(grade.getNumericGrade()));
        }
        
        Grade savedGrade = gradeRepository.save(grade);
        
        // Update enrollment current grade
        updateEnrollmentCurrentGrade(enrollment);
        
        return savedGrade;
    }

    @PreAuthorize("hasRole('FACULTY')")
    public Grade updateGrade(Long gradeId, Grade gradeUpdate) {
        Grade existingGrade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));
        
        Faculty faculty = getCurrentFaculty();
        validateCourseAccess(existingGrade.getCourse().getId(), faculty);
        
        // Update grade fields
        if (gradeUpdate.getGradeType() != null) {
            existingGrade.setGradeType(gradeUpdate.getGradeType());
        }
        if (gradeUpdate.getLetterGrade() != null) {
            existingGrade.setLetterGrade(gradeUpdate.getLetterGrade());
        }
        if (gradeUpdate.getNumericGrade() != null) {
            existingGrade.setNumericGrade(gradeUpdate.getNumericGrade());
            // Auto-update letter grade
            existingGrade.setLetterGrade(calculateLetterGrade(gradeUpdate.getNumericGrade()));
        }
        if (gradeUpdate.getMaxPoints() != null) {
            existingGrade.setMaxPoints(gradeUpdate.getMaxPoints());
        }
        if (gradeUpdate.getPointsEarned() != null) {
            existingGrade.setPointsEarned(gradeUpdate.getPointsEarned());
        }
        if (gradeUpdate.getComments() != null) {
            existingGrade.setComments(gradeUpdate.getComments());
        }
        
        existingGrade.setUpdatedAt(LocalDateTime.now());
        Grade updatedGrade = gradeRepository.save(existingGrade);
        
        // Update enrollment current grade
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(
            existingGrade.getStudent().getId(), existingGrade.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        updateEnrollmentCurrentGrade(enrollment);
        
        return updatedGrade;
    }

    // Bulk Grade Entry
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> enterBulkGrades(Long courseId, List<Map<String, Object>> gradeEntries) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> result = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<Grade> savedGrades = new ArrayList<>();
        
        for (Map<String, Object> entry : gradeEntries) {
            try {
                Long studentId = Long.valueOf(entry.get("studentId").toString());
                String gradeType = (String) entry.get("gradeType");
                String letterGrade = (String) entry.get("letterGrade");
                Double numericGrade = entry.get("numericGrade") != null ? 
                    Double.valueOf(entry.get("numericGrade").toString()) : null;
                Double maxPoints = entry.get("maxPoints") != null ? 
                    Double.valueOf(entry.get("maxPoints").toString()) : null;
                Double pointsEarned = entry.get("pointsEarned") != null ? 
                    Double.valueOf(entry.get("pointsEarned").toString()) : null;
                String comments = (String) entry.get("comments");
                
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
                
                // Verify enrollment
                enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not enrolled in course"));
                
                Grade grade = new Grade();
                grade.setStudent(student);
                grade.setCourse(course);
                grade.setGradeType(gradeType);
                grade.setLetterGrade(letterGrade);
                grade.setNumericGrade(numericGrade);
                grade.setMaxPoints(maxPoints);
                grade.setPointsEarned(pointsEarned);
                grade.setComments(comments);
                grade.setGradeDate(LocalDateTime.now());
                grade.setCreatedAt(LocalDateTime.now());
                
                // Auto-calculate letter grade if needed
                if (grade.getLetterGrade() == null && grade.getNumericGrade() != null) {
                    grade.setLetterGrade(calculateLetterGrade(grade.getNumericGrade()));
                }
                
                Grade savedGrade = gradeRepository.save(grade);
                savedGrades.add(savedGrade);
                successful.add("Grade entered for " + student.getUser().getName());
                
            } catch (Exception e) {
                failed.add("Failed to enter grade: " + e.getMessage());
            }
        }
        
        // Update enrollment current grades for all affected students
        for (Grade grade : savedGrades) {
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(
                grade.getStudent().getId(), grade.getCourse().getId())
                    .orElse(null);
            if (enrollment != null) {
                updateEnrollmentCurrentGrade(enrollment);
            }
        }
        
        result.put("successful", successful);
        result.put("failed", failed);
        result.put("successCount", successful.size());
        result.put("failCount", failed.size());
        result.put("totalProcessed", gradeEntries.size());
        result.put("course", course.getCourseName());
        result.put("enteredBy", faculty.getUser().getName());
        result.put("entryDate", LocalDateTime.now());
        
        return result;
    }

    // Grade Analytics and Reports
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> getCourseGradeAnalytics(Long courseId) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> analytics = new HashMap<>();
        
        List<Grade> courseGrades = gradeRepository.findByCourseId(courseId);
        analytics.put("totalGrades", courseGrades.size());
        
        if (courseGrades.isEmpty()) {
            analytics.put("message", "No grades available for analysis");
            return analytics;
        }
        
        // Grade distribution analysis
        Map<String, Long> letterGradeDistribution = courseGrades.stream()
                .filter(g -> g.getLetterGrade() != null)
                .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));
        analytics.put("letterGradeDistribution", letterGradeDistribution);
        
        // Numeric grade statistics
        List<Double> numericGrades = courseGrades.stream()
                .filter(g -> g.getNumericGrade() != null)
                .map(Grade::getNumericGrade)
                .collect(Collectors.toList());
        
        if (!numericGrades.isEmpty()) {
            DoubleSummaryStatistics stats = numericGrades.stream().mapToDouble(Double::doubleValue).summaryStatistics();
            analytics.put("numericGradeStatistics", Map.of(
                "average", Math.round(stats.getAverage() * 100.0) / 100.0,
                "min", stats.getMin(),
                "max", stats.getMax(),
                "count", stats.getCount()
            ));
            
            // Performance categories
            Map<String, Integer> performanceCategories = new HashMap<>();
            performanceCategories.put("excellent", 0); // 90-100
            performanceCategories.put("good", 0); // 80-89
            performanceCategories.put("satisfactory", 0); // 70-79
            performanceCategories.put("needs_improvement", 0); // 60-69
            performanceCategories.put("failing", 0); // <60
            
            for (Double grade : numericGrades) {
                if (grade >= 90) {
                    performanceCategories.merge("excellent", 1, Integer::sum);
                } else if (grade >= 80) {
                    performanceCategories.merge("good", 1, Integer::sum);
                } else if (grade >= 70) {
                    performanceCategories.merge("satisfactory", 1, Integer::sum);
                } else if (grade >= 60) {
                    performanceCategories.merge("needs_improvement", 1, Integer::sum);
                } else {
                    performanceCategories.merge("failing", 1, Integer::sum);
                }
            }
            analytics.put("performanceCategories", performanceCategories);
            
            // Pass rate calculation
            long passingGrades = numericGrades.stream().filter(g -> g >= 60.0).count();
            double passRate = (double) passingGrades / numericGrades.size() * 100;
            analytics.put("passRate", Math.round(passRate * 100.0) / 100.0);
        }
        
        // Grade type distribution
        Map<String, Long> gradeTypeDistribution = courseGrades.stream()
                .filter(g -> g.getGradeType() != null)
                .collect(Collectors.groupingBy(Grade::getGradeType, Collectors.counting()));
        analytics.put("gradeTypeDistribution", gradeTypeDistribution);
        
        // Top and bottom performers
        List<Map<String, Object>> topPerformers = getTopPerformers(courseId, 5);
        List<Map<String, Object>> bottomPerformers = getBottomPerformers(courseId, 5);
        analytics.put("topPerformers", topPerformers);
        analytics.put("bottomPerformers", bottomPerformers);
        
        // Grading timeline
        List<Map<String, Object>> gradingTimeline = getGradingTimeline(courseId);
        analytics.put("gradingTimeline", gradingTimeline);
        
        return analytics;
    }

    // Grade Reports
    @PreAuthorize("hasRole('FACULTY')")
    public Map<String, Object> generateGradeReport(Long courseId, String reportType) {
        Faculty faculty = getCurrentFaculty();
        Course course = validateCourseAccess(courseId, faculty);
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", reportType);
        report.put("course", Map.of(
            "id", course.getId(),
            "name", course.getCourseName(),
            "code", course.getCourseCode()
        ));
        report.put("generatedBy", faculty.getUser().getName());
        report.put("generatedAt", LocalDateTime.now());
        
        switch (reportType.toLowerCase()) {
            case "final_grades":
                report.putAll(generateFinalGradesReport(courseId));
                break;
            case "grade_distribution":
                report.putAll(generateGradeDistributionReport(courseId));
                break;
            case "student_progress":
                report.putAll(generateStudentProgressReport(courseId));
                break;
            case "comprehensive":
                report.putAll(generateComprehensiveGradeReport(courseId));
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

    private String calculateLetterGrade(Double numericGrade) {
        if (numericGrade >= 97) return "A+";
        else if (numericGrade >= 93) return "A";
        else if (numericGrade >= 90) return "A-";
        else if (numericGrade >= 87) return "B+";
        else if (numericGrade >= 83) return "B";
        else if (numericGrade >= 80) return "B-";
        else if (numericGrade >= 77) return "C+";
        else if (numericGrade >= 73) return "C";
        else if (numericGrade >= 70) return "C-";
        else if (numericGrade >= 67) return "D+";
        else if (numericGrade >= 60) return "D";
        else return "F";
    }

    private void updateEnrollmentCurrentGrade(Enrollment enrollment) {
        List<Grade> studentGrades = gradeRepository.findByStudentIdAndCourseId(
            enrollment.getStudent().getId(), enrollment.getCourse().getId());
        
        if (!studentGrades.isEmpty()) {
            // Calculate weighted average or use most recent grade
            // For simplicity, using most recent grade
            Grade latestGrade = studentGrades.stream()
                    .max(Comparator.comparing(Grade::getGradeDate))
                    .orElse(null);
            
            if (latestGrade != null) {
                enrollment.setCurrentGrade(latestGrade.getLetterGrade());
                enrollmentRepository.save(enrollment);
            }
        }
    }

    private List<Map<String, Object>> getRecentGradingActivity(List<Course> courses, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        for (Course course : courses) {
            List<Grade> recentGrades = gradeRepository.findByCourseIdOrderByGradedDateDesc(course.getId())
                    .stream().limit(limit / courses.size() + 1).toList();
            
            for (Grade grade : recentGrades) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("courseId", course.getId());
                activity.put("courseName", course.getCourseName());
                activity.put("studentName", grade.getStudent().getUser().getName());
                activity.put("gradeType", grade.getGradeType());
                activity.put("letterGrade", grade.getLetterGrade());
                activity.put("numericGrade", grade.getNumericGrade());
                activity.put("gradeDate", grade.getGradeDate());
                activities.add(activity);
            }
        }
        
        // Sort by grade date and limit
        activities.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("gradeDate");
            LocalDateTime dateB = (LocalDateTime) b.get("gradeDate");
            return dateB.compareTo(dateA);
        });
        
        return activities.stream().limit(limit).toList();
    }

    private List<Map<String, Object>> getPendingGradingTasks(List<Course> courses) {
        List<Map<String, Object>> pendingTasks = new ArrayList<>();
        
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            List<Grade> courseGrades = gradeRepository.findByCourseId(course.getId());
            
            Set<Long> gradedStudentIds = courseGrades.stream()
                    .map(g -> g.getStudent().getId())
                    .collect(Collectors.toSet());
            
            long pendingStudents = enrollments.stream()
                    .filter(e -> !gradedStudentIds.contains(e.getStudent().getId()))
                    .count();
            
            if (pendingStudents > 0) {
                Map<String, Object> task = new HashMap<>();
                task.put("courseId", course.getId());
                task.put("courseName", course.getCourseName());
                task.put("courseCode", course.getCourseCode());
                task.put("pendingStudents", pendingStudents);
                task.put("totalStudents", enrollments.size());
                pendingTasks.add(task);
            }
        }
        
        return pendingTasks;
    }

    private List<Map<String, Object>> getTopPerformers(Long courseId, int limit) {
        List<Grade> courseGrades = gradeRepository.findByCourseId(courseId);
        
        Map<Long, Double> studentAverages = courseGrades.stream()
                .filter(g -> g.getNumericGrade() != null)
                .collect(Collectors.groupingBy(
                    g -> g.getStudent().getId(),
                    Collectors.averagingDouble(Grade::getNumericGrade)
                ));
        
        return studentAverages.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Student student = studentRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> performer = new HashMap<>();
                    if (student != null) {
                        performer.put("studentId", student.getId());
                        performer.put("studentName", student.getUser().getName());
                        performer.put("averageGrade", Math.round(entry.getValue() * 100.0) / 100.0);
                    }
                    return performer;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getBottomPerformers(Long courseId, int limit) {
        List<Grade> courseGrades = gradeRepository.findByCourseId(courseId);
        
        Map<Long, Double> studentAverages = courseGrades.stream()
                .filter(g -> g.getNumericGrade() != null)
                .collect(Collectors.groupingBy(
                    g -> g.getStudent().getId(),
                    Collectors.averagingDouble(Grade::getNumericGrade)
                ));
        
        return studentAverages.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .map(entry -> {
                    Student student = studentRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> performer = new HashMap<>();
                    if (student != null) {
                        performer.put("studentId", student.getId());
                        performer.put("studentName", student.getUser().getName());
                        performer.put("averageGrade", Math.round(entry.getValue() * 100.0) / 100.0);
                    }
                    return performer;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getGradingTimeline(Long courseId) {
        List<Grade> courseGrades = gradeRepository.findByCourseIdOrderByGradedDateAsc(courseId);
        
        Map<String, Long> dailyGradeCount = courseGrades.stream()
                .collect(Collectors.groupingBy(
                    g -> g.getGradeDate().toLocalDate().toString(),
                    Collectors.counting()
                ));
        
        return dailyGradeCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey());
                    map.put("gradesEntered", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> generateFinalGradesReport(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> finalGrades = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<Grade> studentGrades = gradeRepository.findByStudentIdAndCourseId(student.getId(), courseId);
            
            Map<String, Object> studentGrade = new HashMap<>();
            studentGrade.put("studentId", student.getId());
            studentGrade.put("studentName", student.getUser().getName());
            studentGrade.put("studentNumber", student.getStudentId());
            
            if (!studentGrades.isEmpty()) {
                double averageGrade = studentGrades.stream()
                        .filter(g -> g.getNumericGrade() != null)
                        .mapToDouble(Grade::getNumericGrade)
                        .average()
                        .orElse(0.0);
                studentGrade.put("finalGrade", Math.round(averageGrade * 100.0) / 100.0);
                studentGrade.put("letterGrade", calculateLetterGrade(averageGrade));
            } else {
                studentGrade.put("finalGrade", "N/A");
                studentGrade.put("letterGrade", "N/A");
            }
            
            finalGrades.add(studentGrade);
        }
        
        return Map.of("finalGrades", finalGrades);
    }

    private Map<String, Object> generateGradeDistributionReport(Long courseId) {
        return getCourseGradeAnalytics(courseId);
    }

    private Map<String, Object> generateStudentProgressReport(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> studentProgress = new ArrayList<>();
        
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<Grade> studentGrades = gradeRepository.findByStudentIdAndCourseIdOrderByGradedDateAsc(student.getId(), courseId);
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("studentId", student.getId());
            progress.put("studentName", student.getUser().getName());
            progress.put("enrollmentDate", enrollment.getEnrollmentDate());
            progress.put("gradeHistory", studentGrades.stream().map(grade -> Map.of(
                "gradeType", grade.getGradeType(),
                "numericGrade", grade.getNumericGrade(),
                "letterGrade", grade.getLetterGrade(),
                "gradeDate", grade.getGradeDate()
            )).collect(Collectors.toList()));
            
            studentProgress.add(progress);
        }
        
        return Map.of("studentProgress", studentProgress);
    }

    private Map<String, Object> generateComprehensiveGradeReport(Long courseId) {
        Map<String, Object> report = new HashMap<>();
        report.putAll(generateFinalGradesReport(courseId));
        report.putAll(generateGradeDistributionReport(courseId));
        report.putAll(generateStudentProgressReport(courseId));
        return report;
    }
}