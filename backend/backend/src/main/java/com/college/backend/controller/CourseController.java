package com.college.backend.controller;

import com.college.backend.dto.ApiResponse;
import com.college.backend.model.Course;
import com.college.backend.model.Enrollment;
import com.college.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500", "*"})
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Course Management
    @GetMapping
    public ResponseEntity<?> getAllCourses(Pageable pageable) {
        try {
            Page<Course> courses = courseService.getAllCourses(pageable);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllCoursesList() {
        try {
            List<Course> courses = courseService.getAllCourses();
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Course not found: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<?> getCourseByCourseCode(@PathVariable String courseCode) {
        try {
            Course course = courseService.getCourseByCourseCode(courseCode);
            return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Course not found: " + e.getMessage()));
        }
    }

    // Course Queries
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getCoursesByDepartment(@PathVariable Long departmentId) {
        try {
            List<Course> courses = courseService.getCoursesByDepartment(departmentId);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/department/code/{departmentCode}")
    public ResponseEntity<?> getCoursesByDepartmentCode(@PathVariable String departmentCode) {
        try {
            List<Course> courses = courseService.getCoursesByDepartmentCode(departmentCode);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<?> getCoursesByInstructor(@PathVariable Long instructorId) {
        try {
            List<Course> courses = courseService.getCoursesByInstructor(instructorId);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/available-enrollment")
    public ResponseEntity<?> getCoursesAvailableForEnrollment() {
        try {
            List<Course> courses = courseService.getCoursesAvailableForEnrollment();
            return ResponseEntity.ok(ApiResponse.success("Available courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve courses: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCourses(@RequestParam String searchTerm) {
        try {
            List<Course> courses = courseService.searchCourses(searchTerm);
            return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search courses: " + e.getMessage()));
        }
    }

    // Enrollment Related
    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<?> getCourseEnrollments(@PathVariable Long courseId) {
        try {
            List<Enrollment> enrollments = courseService.getCourseEnrollments(courseId);
            return ResponseEntity.ok(ApiResponse.success("Course enrollments retrieved successfully", enrollments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollments: " + e.getMessage()));
        }
    }

    @GetMapping("/{courseId}/enrollment-count")
    public ResponseEntity<?> getCourseEnrollmentCount(@PathVariable Long courseId) {
        try {
            long count = courseService.getCourseEnrollmentCount(courseId);
            return ResponseEntity.ok(ApiResponse.success("Enrollment count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve enrollment count: " + e.getMessage()));
        }
    }

    // Statistics
    @GetMapping("/count")
    public ResponseEntity<?> getTotalCourseCount() {
        try {
            long count = courseService.getTotalCourseCount();
            return ResponseEntity.ok(ApiResponse.success("Course count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve count: " + e.getMessage()));
        }
    }
}
