package com.college.backend.service;

import com.college.backend.model.Faculty;
import com.college.backend.model.User;
import com.college.backend.model.Course;
import com.college.backend.model.Grade;
import com.college.backend.model.AttendanceRecord;
import com.college.backend.model.Department;
import com.college.backend.model.Enrollment;
import com.college.backend.model.Student;
import com.college.backend.repository.FacultyRepository;
import com.college.backend.repository.UserRepository;
import com.college.backend.repository.CourseRepository;
import com.college.backend.repository.GradeRepository;
import com.college.backend.repository.AttendanceRecordRepository;
import com.college.backend.repository.DepartmentRepository;
import com.college.backend.repository.EnrollmentRepository;
import com.college.backend.repository.StudentRepository;
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
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    // Faculty Profile Management
    @PreAuthorize("hasRole('ADMIN') or (hasRole('FACULTY') and #userId == authentication.principal.id)")
    public Faculty getFacultyByUserId(Long userId) {
        return facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for user ID: " + userId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Faculty getFacultyById(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + facultyId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Faculty getFacultyByEmployeeId(String employeeId) {
        return facultyRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with employee ID: " + employeeId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Faculty createFaculty(Faculty faculty) {
        validateFaculty(faculty);
        
        // Validate user exists
        User user = userRepository.findById(faculty.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Set user role to FACULTY if not already
        if (user.getRole() != User.UserRole.FACULTY) {
            user.setRole(User.UserRole.FACULTY);
            userRepository.save(user);
        }
        
        faculty.setUser(user);
        return facultyRepository.save(faculty);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('FACULTY') and #faculty.user.id == authentication.principal.id)")
    public Faculty updateFaculty(Faculty faculty) {
        Faculty existingFaculty = getFacultyById(faculty.getId());
        
        // Update allowed fields
        existingFaculty.setPhone(faculty.getPhone());
        existingFaculty.setAddress(faculty.getAddress());
        existingFaculty.setEmergencyContact(faculty.getEmergencyContact());
        existingFaculty.setResearchInterests(faculty.getResearchInterests());
        existingFaculty.setPublications(faculty.getPublications());
        
        // Admin-only updates
        if (hasAdminRole()) {
            existingFaculty.setStatus(faculty.getStatus());
            existingFaculty.setDesignation(faculty.getDesignation());
            existingFaculty.setDepartment(faculty.getDepartment());
            existingFaculty.setSalary(faculty.getSalary());
            existingFaculty.setEmploymentType(faculty.getEmploymentType());
        }
        
        return facultyRepository.save(existingFaculty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFaculty(Long facultyId) {
        Faculty faculty = getFacultyById(facultyId);
        facultyRepository.delete(faculty);
    }

    // Course Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Course> getFacultyCourses(Long facultyId) {
        return courseRepository.findByInstructorId(facultyId);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('FACULTY') and #facultyId == authentication.principal.id)")
    public Course assignCourseToFaculty(Long facultyId, Long courseId) {
        Faculty faculty = getFacultyById(facultyId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        course.setInstructor(faculty);
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('FACULTY') and #facultyId == authentication.principal.id)")
    public void removeCourseFromFaculty(Long facultyId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        if (course.getInstructor() != null && course.getInstructor().getId().equals(facultyId)) {
            course.setInstructor(null);
            courseRepository.save(course);
        }
    }

    // Grade Management
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public Grade createGrade(Grade grade) {
        validateGradePermission(grade);
        return gradeRepository.save(grade);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public Grade updateGrade(Grade grade) {
        validateGradePermission(grade);
        Grade existingGrade = gradeRepository.findById(grade.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));
        
        existingGrade.setMarksObtained(grade.getMarksObtained());
        existingGrade.setRemarks(grade.getRemarks());
        existingGrade.setGradedDate(grade.getGradedDate());
        
        return gradeRepository.save(existingGrade);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public List<Grade> getGradesByCourse(Long courseId) {
        return gradeRepository.findByCourseId(courseId);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public List<Grade> getGradesByCourseAndAssessment(Long courseId, Grade.AssessmentType assessmentType) {
        return gradeRepository.findByCourseAndAssessmentType(courseId, assessmentType);
    }

    // Attendance Management
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public AttendanceRecord markAttendance(AttendanceRecord attendance) {
        validateAttendancePermission(attendance);
        return attendanceRecordRepository.save(attendance);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public List<AttendanceRecord> markBulkAttendance(List<AttendanceRecord> attendanceRecords) {
        // Validate all records
        attendanceRecords.forEach(this::validateAttendancePermission);
        return attendanceRecordRepository.saveAll(attendanceRecords);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public List<AttendanceRecord> getAttendanceByCourse(Long courseId) {
        return attendanceRecordRepository.findByCourseId(courseId);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public List<AttendanceRecord> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {
        return attendanceRecordRepository.findByCourseAndDate(courseId, date);
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public Long getAttendanceSummaryByCourse(Long courseId) {
        return attendanceRecordRepository.getAttendanceSummaryByCourse(courseId);
    }

    // Administrative Queries
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Faculty> getAllFaculty(Pageable pageable) {
        return facultyRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Faculty> getFacultyByDepartment(Long departmentId) {
        return facultyRepository.findByDepartmentId(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Faculty> getFacultyByStatus(Faculty.FacultyStatus status) {
        return facultyRepository.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Faculty> getFacultyByDesignation(Faculty.Designation designation) {
        return facultyRepository.findByDesignation(designation);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Faculty> searchFaculty(String searchTerm) {
        return facultyRepository.searchFaculty(searchTerm);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Faculty> getFacultyByExperienceRange(Integer minYears, Integer maxYears) {
        return facultyRepository.findByExperienceYearsBetween(minYears, maxYears);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Faculty> getFacultyByQualification(String qualification) {
        return facultyRepository.findByQualification(qualification);
    }

    // Department Head Management
    @PreAuthorize("hasRole('ADMIN')")
    public Faculty assignAsDepartmentHead(Long facultyId, Long departmentId) {
        Faculty faculty = getFacultyById(facultyId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        // Remove current head if exists
        if (department.getHeadOfDepartment() != null) {
            Faculty currentHead = department.getHeadOfDepartment();
            currentHead.setIsDepartmentHead(false);
            facultyRepository.save(currentHead);
        }
        
        // Assign new head
        faculty.setIsDepartmentHead(true);
        department.setHeadOfDepartment(faculty);
        
        facultyRepository.save(faculty);
        departmentRepository.save(department);
        
        return faculty;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void removeDepartmentHead(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        if (department.getHeadOfDepartment() != null) {
            Faculty currentHead = department.getHeadOfDepartment();
            currentHead.setIsDepartmentHead(false);
            facultyRepository.save(currentHead);
            
            department.setHeadOfDepartment(null);
            departmentRepository.save(department);
        }
    }

    // Statistics and Analytics
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalFacultyCount() {
        return facultyRepository.count();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getFacultyCountByStatus(Faculty.FacultyStatus status) {
        return facultyRepository.countByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getFacultyCountByDepartment(Long departmentId) {
        return facultyRepository.countByDepartmentId(departmentId);
    }

    // Validation Methods
    private void validateFaculty(Faculty faculty) {
        if (faculty.getUser() == null) {
            throw new IllegalArgumentException("Faculty must be associated with a user");
        }
        
        if (faculty.getEmployeeId() == null || faculty.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        
        // Check for duplicate employee ID
        Optional<Faculty> existingFaculty = facultyRepository.findByEmployeeId(faculty.getEmployeeId());
        if (existingFaculty.isPresent() && !existingFaculty.get().getId().equals(faculty.getId())) {
            throw new IllegalArgumentException("Employee ID already exists");
        }
    }

    private void validateGradePermission(Grade grade) {
        // Check if current user is instructor of the course
        // Implementation would check authentication context
        // Simplified for now
    }

    private void validateAttendancePermission(AttendanceRecord attendance) {
        // Check if current user is instructor of the course
        // Implementation would check authentication context
        // Simplified for now
    }

    private boolean hasAdminRole() {
        // Implementation would check current user's role
        // This is a placeholder - actual implementation would use SecurityContextHolder
        return true; // Simplified for now
    }
    
    // Missing methods for FacultyController
    @PreAuthorize("hasRole('FACULTY')")
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }
    
    @PreAuthorize("hasRole('FACULTY')")
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }
    
    @PreAuthorize("hasRole('FACULTY')")
    public List<Student> searchStudents(String searchTerm) {
        return studentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm);
    }
}