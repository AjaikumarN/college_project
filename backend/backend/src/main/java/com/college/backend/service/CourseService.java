package com.college.backend.service;

import com.college.backend.model.Course;
import com.college.backend.model.Department;
import com.college.backend.model.Faculty;
import com.college.backend.model.Enrollment;
import com.college.backend.model.Student;
import com.college.backend.repository.CourseRepository;
import com.college.backend.repository.DepartmentRepository;
import com.college.backend.repository.FacultyRepository;
import com.college.backend.repository.EnrollmentRepository;
import com.college.backend.repository.StudentRepository;
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
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    // Course Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public Course getCourseByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Course createCourse(Course course) {
        validateCourse(course);
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Course updateCourse(Long courseId, Course courseUpdates) {
        Course existingCourse = getCourseById(courseId);
        
        // Update fields
        existingCourse.setCourseName(courseUpdates.getCourseName());
        existingCourse.setDescription(courseUpdates.getDescription());
        existingCourse.setCredits(courseUpdates.getCredits());
        existingCourse.setSemester(courseUpdates.getSemester());
        existingCourse.setAcademicYear(courseUpdates.getAcademicYear());
        existingCourse.setEnrollmentLimit(courseUpdates.getEnrollmentLimit());
        existingCourse.setPrerequisites(courseUpdates.getPrerequisites());
        existingCourse.setType(courseUpdates.getType());
        existingCourse.setStatus(courseUpdates.getStatus());
        
        return courseRepository.save(existingCourse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);
        
        // Check if course has active enrollments
        long enrollmentCount = enrollmentRepository.countByCourseAndStatus(courseId, Enrollment.EnrollmentStatus.ENROLLED);
        if (enrollmentCount > 0) {
            throw new IllegalStateException("Cannot delete course with active enrollments");
        }
        
        courseRepository.delete(course);
    }

    // Instructor Assignment
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Course assignInstructor(Long courseId, Long facultyId) {
        Course course = getCourseById(courseId);
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        course.setInstructor(faculty);
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Course removeInstructor(Long courseId) {
        Course course = getCourseById(courseId);
        course.setInstructor(null);
        return courseRepository.save(course);
    }

    // Department Assignment
    @PreAuthorize("hasRole('ADMIN')")
    public Course assignToDepartment(Long courseId, Long departmentId) {
        Course course = getCourseById(courseId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        course.setDepartment(department);
        return courseRepository.save(course);
    }

    // Enrollment Management
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public Enrollment enrollStudent(Long courseId, Long studentId) {
        Course course = getCourseById(courseId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Check if course is available for enrollment
        if (course.getStatus() != Course.CourseStatus.ACTIVE) {
            throw new IllegalStateException("Course is not available for enrollment");
        }
        
        // Check enrollment limit
        long currentEnrollments = enrollmentRepository.countByCourseAndStatus(courseId, Enrollment.EnrollmentStatus.ENROLLED);
        if (currentEnrollments >= course.getEnrollmentLimit()) {
            throw new IllegalStateException("Course enrollment limit reached");
        }
        
        // Check if student is already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existingEnrollment.isPresent()) {
            throw new IllegalStateException("Student is already enrolled in this course");
        }
        
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

    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public void unenrollStudent(Long courseId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public long getCourseEnrollmentCount(Long courseId) {
        return enrollmentRepository.countByCourseAndStatus(courseId, Enrollment.EnrollmentStatus.ENROLLED);
    }

    // Course Queries
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByDepartmentCode(String departmentCode) {
        return courseRepository.findByDepartmentCode(departmentCode);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByCredits(Integer credits) {
        return courseRepository.findByCredits(credits);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesBySemester(Integer semester) {
        return courseRepository.findBySemester(semester);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByAcademicYear(String academicYear) {
        return courseRepository.findByAcademicYear(academicYear);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByStatus(Course.CourseStatus status) {
        return courseRepository.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByType(Course.CourseType type) {
        return courseRepository.findByType(type);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesAvailableForEnrollment() {
        return courseRepository.findAvailableForEnrollment();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Course> getFullyCourses() {
        return courseRepository.findFullyCourse();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> searchCourses(String searchTerm) {
        return courseRepository.searchCourses(searchTerm);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByCreditRange(Integer minCredits, Integer maxCredits) {
        return courseRepository.findByCreditRange(minCredits, maxCredits);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesWithPrerequisites() {
        return courseRepository.findCoursesWithPrerequisites();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesWithoutPrerequisites() {
        return courseRepository.findCoursesWithoutPrerequisites();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesByPrerequisite(String prerequisite) {
        return courseRepository.findByPrerequisite(prerequisite);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY') or hasRole('STUDENT')")
    public List<Course> getCoursesBySemesterAndYear(Integer semester, String academicYear) {
        return courseRepository.findBySemesterAndAcademicYear(semester, academicYear);
    }

    // Statistics
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public long getTotalCourseCount() {
        return courseRepository.count();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public long getCourseCountByStatus(Course.CourseStatus status) {
        return courseRepository.countByStatus(status);
    }

    // Course Status Management
    @PreAuthorize("hasRole('ADMIN')")
    public Course activateCourse(Long courseId) {
        Course course = getCourseById(courseId);
        course.setStatus(Course.CourseStatus.ACTIVE);
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course deactivateCourse(Long courseId) {
        Course course = getCourseById(courseId);
        course.setStatus(Course.CourseStatus.INACTIVE);
        return courseRepository.save(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course archiveCourse(Long courseId) {
        Course course = getCourseById(courseId);
        course.setStatus(Course.CourseStatus.ARCHIVED);
        return courseRepository.save(course);
    }

    // Validation Methods
    private void validateCourse(Course course) {
        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }
        
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }
        
        if (course.getCredits() == null || course.getCredits() <= 0) {
            throw new IllegalArgumentException("Credits must be greater than 0");
        }
        
        if (course.getEnrollmentLimit() == null || course.getEnrollmentLimit() <= 0) {
            throw new IllegalArgumentException("Enrollment limit must be greater than 0");
        }
        
        // Check for duplicate course code
        Optional<Course> existingCourse = courseRepository.findByCourseCode(course.getCourseCode());
        if (existingCourse.isPresent() && !existingCourse.get().getId().equals(course.getId())) {
            throw new IllegalArgumentException("Course code already exists");
        }
    }
}