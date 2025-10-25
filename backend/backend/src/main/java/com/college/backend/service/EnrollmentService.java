package com.college.backend.service;

import com.college.backend.model.Enrollment;
import com.college.backend.model.Student;
import com.college.backend.model.Course;
import com.college.backend.repository.EnrollmentRepository;
import com.college.backend.repository.StudentRepository;
import com.college.backend.repository.CourseRepository;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    // Enrollment Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Page<Enrollment> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or (hasRole('STUDENT') and #enrollment.student.user.id == authentication.principal.id)")
    public Enrollment getEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
    }

    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public Enrollment createEnrollment(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Validate enrollment
        validateEnrollment(studentId, courseId);
        
        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setAcademicYear(course.getAcademicYear());
        enrollment.setSemester(course.getSemester());
        
        return enrollmentRepository.save(enrollment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Enrollment updateEnrollment(Long enrollmentId, Enrollment enrollmentUpdates) {
        Enrollment existingEnrollment = getEnrollmentById(enrollmentId);
        
        // Update allowed fields
        existingEnrollment.setStatus(enrollmentUpdates.getStatus());
        existingEnrollment.setFinalGrade(enrollmentUpdates.getFinalGrade());
        existingEnrollment.setGradePoints(enrollmentUpdates.getGradePoints());
        
        return enrollmentRepository.save(existingEnrollment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        enrollmentRepository.delete(enrollment);
    }

    // Student Enrollment Operations
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public List<Enrollment> getStudentEnrollmentsByAcademicYear(Long studentId, String academicYear) {
        return enrollmentRepository.findByStudentAndAcademicYear(studentId, academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public List<Enrollment> getStudentEnrollmentsBySemester(Long studentId, Integer semester, String academicYear) {
        return enrollmentRepository.findByStudentSemesterAndYear(studentId, semester, academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public Integer getStudentTotalCredits(Long studentId, String academicYear, Integer semester) {
        return enrollmentRepository.getTotalCreditsForStudentInSemester(studentId, academicYear, semester);
    }

    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public Enrollment dropCourse(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        // Validate drop operation
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Cannot drop course - enrollment status is not ENROLLED");
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        return enrollmentRepository.save(enrollment);
    }

    // Course Enrollment Operations
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public long getCourseEnrollmentCount(Long courseId, Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.countByCourseAndStatus(courseId, status);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsByStatus(Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsByAcademicYear(String academicYear) {
        return enrollmentRepository.findByAcademicYear(academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsBySemester(Integer semester) {
        return enrollmentRepository.findBySemester(semester);
    }

    // Grade Management
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public Enrollment assignFinalGrade(Long enrollmentId, String finalGrade) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        
        enrollment.setFinalGrade(finalGrade);
        enrollment.setGradePoints(calculateGradePoints(finalGrade));
        
        return enrollmentRepository.save(enrollment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getPendingGrades() {
        return enrollmentRepository.findPendingGrades();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getCompletedEnrollments() {
        return enrollmentRepository.findCompletedEnrollments();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public List<Enrollment> getCompletedEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findCompletedEnrollmentsByStudent(studentId);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public Double calculateStudentGPA(Long studentId) {
        return enrollmentRepository.calculateGPAForStudent(studentId);
    }

    // Department and Instructor specific queries
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsByDepartment(Long departmentId) {
        return enrollmentRepository.findByDepartment(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsByInstructor(Long instructorId) {
        return enrollmentRepository.findByInstructor(instructorId);
    }

    // Time-based queries
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getEnrollmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentRepository.findByEnrollmentDateBetween(startDate, endDate);
    }

    // Statistics and Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalEnrollmentCount() {
        return enrollmentRepository.count();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getEnrollmentCountByStatus(Enrollment.EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.findByStatus(status);
        return enrollments.size();
    }

    // Validation Methods
    private void validateEnrollment(Long studentId, Long courseId) {
        // Check if student is already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existingEnrollment.isPresent() && existingEnrollment.get().getStatus() == Enrollment.EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }
        
        // Check course enrollment limit
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        long currentEnrollments = enrollmentRepository.countByCourseAndStatus(courseId, Enrollment.EnrollmentStatus.ENROLLED);
        if (currentEnrollments >= course.getEnrollmentLimit()) {
            throw new IllegalStateException("Course enrollment limit has been reached");
        }
        
        // Check course status
        if (course.getStatus() != Course.CourseStatus.ACTIVE) {
            throw new IllegalStateException("Course is not available for enrollment");
        }
        
        // Check student status
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        if (student.getStatus() != Student.StudentStatus.ACTIVE) {
            throw new IllegalStateException("Student is not in active status");
        }
    }

    private Double calculateGradePoints(String grade) {
        // Grade point calculation based on grade
        switch (grade.toUpperCase()) {
            case "A+": return 10.0;
            case "A": return 9.0;
            case "B+": return 8.0;
            case "B": return 7.0;
            case "C+": return 6.0;
            case "C": return 5.0;
            case "D": return 4.0;
            case "F": return 0.0;
            default: return null;
        }
    }
}