package com.college.backend.service;

import com.college.backend.model.Student;
import com.college.backend.model.User;
import com.college.backend.model.Enrollment;
import com.college.backend.model.Grade;
import com.college.backend.model.AttendanceRecord;
import com.college.backend.repository.StudentRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.repository.EnrollmentRepository;
import com.college.backend.repository.GradeRepository;
import com.college.backend.repository.AttendanceRecordRepository;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    // Student Profile Management
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #userId == authentication.principal.id)")
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user ID: " + userId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with student ID: " + studentId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Student createStudent(Student student) {
        // Validate user exists
        User user = userRepository.findById(student.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Set user role to STUDENT if not already
        if (user.getRole() != User.UserRole.STUDENT) {
            user.setRole(User.UserRole.STUDENT);
            userRepository.save(user);
        }
        
        student.setUser(user);
        return studentRepository.save(student);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #student.user.id == authentication.principal.id)")
    public Student updateStudent(Student student) {
        Student existingStudent = getStudentById(student.getId());
        
        // Update allowed fields
        existingStudent.setPhone(student.getPhone());
        existingStudent.setAddress(student.getAddress());
        existingStudent.setEmergencyContact(student.getEmergencyContact());
        existingStudent.setBloodGroup(student.getBloodGroup());
        
        // Admin-only updates
        if (hasAdminRole()) {
            existingStudent.setStatus(student.getStatus());
            existingStudent.setAcademicYear(student.getAcademicYear());
            existingStudent.setSemester(student.getSemester());
            existingStudent.setFeeStatus(student.getFeeStatus());
            existingStudent.setHostelResident(student.getHostelResident());
        }
        
        return studentRepository.save(existingStudent);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteStudent(Long studentId) {
        Student student = getStudentById(studentId);
        studentRepository.delete(student);
    }

    // Academic Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public List<Enrollment> getStudentEnrollmentsByAcademicYear(Long studentId, String academicYear) {
        return enrollmentRepository.findByStudentAndAcademicYear(studentId, academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public List<Grade> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public List<Grade> getStudentGradesByAcademicYear(Long studentId, String academicYear) {
        return gradeRepository.findByStudentAndAcademicYear(studentId, academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public Double getStudentGPA(Long studentId) {
        return gradeRepository.calculateGPAForStudent(studentId);
    }

    // Attendance Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<AttendanceRecord> getStudentAttendance(Long studentId) {
        return attendanceRecordRepository.findByStudentId(studentId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<AttendanceRecord> getStudentAttendanceByDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.findByStudentAndDateRange(studentId, startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public Double getStudentAttendancePercentage(Long studentId, Long courseId) {
        return attendanceRecordRepository.calculateAttendancePercentage(studentId, courseId);
    }

    // Administrative Queries
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Student> getStudentsByStatus(Student.StudentStatus status) {
        return studentRepository.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Student> getStudentsByAcademicYear(String academicYear) {
        return studentRepository.findByAcademicYear(academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Student> getStudentsBySemester(Integer semester) {
        return studentRepository.findBySemester(semester);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Student> searchStudents(String searchTerm) {
        return studentRepository.searchStudents(searchTerm);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Student> getStudentsByFeeStatus(Student.FeeStatus feeStatus) {
        return studentRepository.findByFeeStatus(feeStatus);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Student> getHostelResidents() {
        return studentRepository.findByHostelResident(true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Student> getHighPerformingStudents(Double minCGPA) {
        return studentRepository.findByCgpaGreaterThanEqual(minCGPA);
    }

    // Statistics and Analytics
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public long getTotalStudentCount() {
        return studentRepository.count();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getStudentCountByStatus(Student.StudentStatus status) {
        return studentRepository.countByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getStudentCountByFeeStatus(Student.FeeStatus feeStatus) {
        return studentRepository.countByFeeStatus(feeStatus);
    }

    // Helper method to check admin role
    private boolean hasAdminRole() {
        // Implementation would check current user's role
        // This is a placeholder - actual implementation would use SecurityContextHolder
        return true; // Simplified for now
    }

    // Validation Methods
    private void validateStudent(Student student) {
        if (student.getUser() == null) {
            throw new IllegalArgumentException("Student must be associated with a user");
        }
        
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        
        // Check for duplicate student ID
        Optional<Student> existingStudent = studentRepository.findByStudentId(student.getStudentId());
        if (existingStudent.isPresent() && !existingStudent.get().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }
    }
}