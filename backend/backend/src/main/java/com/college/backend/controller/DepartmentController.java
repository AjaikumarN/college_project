package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.model.Department;
import com.college.backend.model.Faculty;
import com.college.backend.model.Course;
import com.college.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"})
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // Department Management
    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long departmentId) {
        try {
            Department department = departmentService.getDepartmentById(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Department not found: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{departmentCode}")
    public ResponseEntity<?> getDepartmentByCode(@PathVariable String departmentCode) {
        try {
            Department department = departmentService.getDepartmentByCode(departmentCode);
            return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Department not found: " + e.getMessage()));
        }
    }

    @GetMapping("/name/{departmentName}")
    public ResponseEntity<?> getDepartmentByName(@PathVariable String departmentName) {
        try {
            Department department = departmentService.getDepartmentByName(departmentName);
            return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Department not found: " + e.getMessage()));
        }
    }

    // Faculty Management within Department
    @GetMapping("/{departmentId}/faculty")
    public ResponseEntity<?> getDepartmentFaculty(@PathVariable Long departmentId) {
        try {
            List<Faculty> faculty = departmentService.getDepartmentFaculty(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department faculty retrieved successfully", faculty));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve faculty: " + e.getMessage()));
        }
    }

    // Course Management within Department
    @GetMapping("/{departmentId}/courses")
    public ResponseEntity<?> getDepartmentCourses(@PathVariable Long departmentId) {
        try {
            List<Course> courses = departmentService.getDepartmentCourses(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Department courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    // Department Queries
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getDepartmentsByStatus(@PathVariable Department.DepartmentStatus status) {
        try {
            List<Department> departments = departmentService.getDepartmentsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDepartments(@RequestParam String searchTerm) {
        try {
            List<Department> departments = departmentService.searchDepartments(searchTerm);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search departments: " + e.getMessage()));
        }
    }

    @GetMapping("/without-head")
    public ResponseEntity<?> getDepartmentsWithoutHead() {
        try {
            List<Department> departments = departmentService.getDepartmentsWithoutHead();
            return ResponseEntity.ok(ApiResponse.success("Departments without head retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/min-faculty/{minSize}")
    public ResponseEntity<?> getDepartmentsByMinimumFacultySize(@PathVariable int minSize) {
        try {
            List<Department> departments = departmentService.getDepartmentsByMinimumFacultySize(minSize);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/min-courses/{minCourses}")
    public ResponseEntity<?> getDepartmentsByMinimumCourseCount(@PathVariable int minCourses) {
        try {
            List<Department> departments = departmentService.getDepartmentsByMinimumCourseCount(minCourses);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/established/{year}")
    public ResponseEntity<?> getDepartmentsByEstablishedYear(@PathVariable Integer year) {
        try {
            List<Department> departments = departmentService.getDepartmentsByEstablishedYear(year);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/established-range")
    public ResponseEntity<?> getDepartmentsByYearRange(@RequestParam Integer startYear, @RequestParam Integer endYear) {
        try {
            List<Department> departments = departmentService.getDepartmentsByYearRange(startYear, endYear);
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/by-faculty-count")
    public ResponseEntity<?> getDepartmentsByFacultyCountDesc() {
        try {
            List<Department> departments = departmentService.getDepartmentsByFacultyCountDesc();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    @GetMapping("/by-course-count")
    public ResponseEntity<?> getDepartmentsByCourseCountDesc() {
        try {
            List<Department> departments = departmentService.getDepartmentsByCourseCountDesc();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve departments: " + e.getMessage()));
        }
    }

    // Statistics
    @GetMapping("/count")
    public ResponseEntity<?> getTotalDepartmentCount() {
        try {
            long count = departmentService.getTotalDepartmentCount();
            return ResponseEntity.ok(ApiResponse.success("Department count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve count: " + e.getMessage()));
        }
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<?> getDepartmentCountByStatus(@PathVariable Department.DepartmentStatus status) {
        try {
            long count = departmentService.getDepartmentCountByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Department count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve count: " + e.getMessage()));
        }
    }
}