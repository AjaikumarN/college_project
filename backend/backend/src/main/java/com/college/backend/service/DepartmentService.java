package com.college.backend.service;

import com.college.backend.model.Department;
import com.college.backend.model.Faculty;
import com.college.backend.model.Course;
import com.college.backend.repository.DepartmentRepository;
import com.college.backend.repository.FacultyRepository;
import com.college.backend.repository.CourseRepository;
import com.college.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    // Department Management
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Department getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Department getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + departmentCode));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public Department getDepartmentByName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name: " + departmentName));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(Department department) {
        validateDepartment(department);
        return departmentRepository.save(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department updateDepartment(Long departmentId, Department departmentUpdates) {
        Department existingDepartment = getDepartmentById(departmentId);
        
        // Update fields
        existingDepartment.setDepartmentName(departmentUpdates.getDepartmentName());
        existingDepartment.setDescription(departmentUpdates.getDescription());
        existingDepartment.setStatus(departmentUpdates.getStatus());
        existingDepartment.setEstablishedYear(departmentUpdates.getEstablishedYear());
        existingDepartment.setLocation(departmentUpdates.getLocation());
        existingDepartment.setContactEmail(departmentUpdates.getContactEmail());
        existingDepartment.setContactPhone(departmentUpdates.getContactPhone());
        
        return departmentRepository.save(existingDepartment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDepartment(Long departmentId) {
        Department department = getDepartmentById(departmentId);
        
        // Check if department has faculty members
        List<Faculty> facultyMembers = facultyRepository.findByDepartmentId(departmentId);
        if (!facultyMembers.isEmpty()) {
            throw new IllegalStateException("Cannot delete department with active faculty members");
        }
        
        // Check if department has courses
        List<Course> courses = courseRepository.findByDepartmentId(departmentId);
        if (!courses.isEmpty()) {
            throw new IllegalStateException("Cannot delete department with active courses");
        }
        
        departmentRepository.delete(department);
    }

    // Department Head Management
    @PreAuthorize("hasRole('ADMIN')")
    public Department assignDepartmentHead(Long departmentId, Long facultyId) {
        Department department = getDepartmentById(departmentId);
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Validate faculty belongs to this department
        if (!faculty.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Faculty must belong to the department to be assigned as head");
        }
        
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
        return departmentRepository.save(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department removeDepartmentHead(Long departmentId) {
        Department department = getDepartmentById(departmentId);
        
        if (department.getHeadOfDepartment() != null) {
            Faculty currentHead = department.getHeadOfDepartment();
            currentHead.setIsDepartmentHead(false);
            facultyRepository.save(currentHead);
            
            department.setHeadOfDepartment(null);
            return departmentRepository.save(department);
        }
        
        return department;
    }

    // Faculty Management within Department
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Faculty> getDepartmentFaculty(Long departmentId) {
        return facultyRepository.findByDepartmentId(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Faculty addFacultyToDepartment(Long departmentId, Long facultyId) {
        Department department = getDepartmentById(departmentId);
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        faculty.setDepartment(department);
        return facultyRepository.save(faculty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Faculty removeFacultyFromDepartment(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Check if faculty is department head
        if (faculty.getIsDepartmentHead()) {
            throw new IllegalStateException("Cannot remove department head. Assign new head first.");
        }
        
        faculty.setDepartment(null);
        return facultyRepository.save(faculty);
    }

    // Course Management within Department
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Course> getDepartmentCourses(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Course addCourseToDepartment(Long departmentId, Long courseId) {
        Department department = getDepartmentById(departmentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        course.setDepartment(department);
        return courseRepository.save(course);
    }

    // Department Queries and Statistics
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Department> getDepartmentsByStatus(Department.DepartmentStatus status) {
        return departmentRepository.findByStatus(status);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public List<Department> searchDepartments(String searchTerm) {
        return departmentRepository.searchDepartments(searchTerm);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsWithoutHead() {
        return departmentRepository.findDepartmentsWithoutHead();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByMinimumFacultySize(int minSize) {
        return departmentRepository.findByMinimumFacultySize(minSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByMinimumCourseCount(int minCourses) {
        return departmentRepository.findByMinimumCourseCount(minCourses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByEstablishedYear(Integer year) {
        return departmentRepository.findByEstablishedYear(year);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByYearRange(Integer startYear, Integer endYear) {
        return departmentRepository.findByEstablishedYearBetween(startYear, endYear);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByFacultyCountDesc() {
        return departmentRepository.findByFacultyCountDesc();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getDepartmentsByCourseCountDesc() {
        return departmentRepository.findByCourseCountDesc();
    }

    // Statistics
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalDepartmentCount() {
        return departmentRepository.count();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public long getDepartmentCountByStatus(Department.DepartmentStatus status) {
        return departmentRepository.countByStatus(status);
    }

    // Validation Methods
    private void validateDepartment(Department department) {
        if (department.getDepartmentCode() == null || department.getDepartmentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Department code is required");
        }
        
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }
        
        // Check for duplicate department code
        Optional<Department> existingByCode = departmentRepository.findByDepartmentCode(department.getDepartmentCode());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Department code already exists");
        }
        
        // Check for duplicate department name
        Optional<Department> existingByName = departmentRepository.findByDepartmentName(department.getDepartmentName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Department name already exists");
        }
    }
}