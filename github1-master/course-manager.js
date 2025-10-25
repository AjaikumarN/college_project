// Course Management JavaScript
// Enhanced course functionality with proper backend integration

class CourseManager {
    constructor() {
        this.currentUser = null;
        this.courses = [];
        this.enrolledCourses = [];
        this.availableCourses = [];
        
        this.initializeAuth();
        this.initializeEventListeners();
        this.loadCourseData();
    }

    // Check authentication and initialize user data
    initializeAuth() {
        if (!authService.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        this.currentUser = authService.getCurrentUser();
        this.renderUserInfo();
    }

    // Initialize event listeners
    initializeEventListeners() {
        // Navigation active state
        this.setActiveNavigation();
        
        // Course enrollment button
        const enrollButtons = document.querySelectorAll('.enroll-btn');
        enrollButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const courseId = e.target.dataset.courseId;
                this.enrollInCourse(courseId);
            });
        });

        // Course unenrollment button
        const unenrollButtons = document.querySelectorAll('.unenroll-btn');
        unenrollButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const courseId = e.target.dataset.courseId;
                this.unenrollFromCourse(courseId);
            });
        });

        // Search functionality
        const searchInput = document.getElementById('course-search');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
        }

        // Filter functionality
        const filterSelects = document.querySelectorAll('.filter-select');
        filterSelects.forEach(select => {
            select.addEventListener('change', () => this.applyFilters());
        });

        // Refresh data
        const refreshButton = document.getElementById('refresh-courses-btn');
        if (refreshButton) {
            refreshButton.addEventListener('click', () => this.refreshData());
        }
    }

    // Load all course data based on user role
    async loadCourseData() {
        try {
            const user = this.currentUser;
            
            if (user.role === 'STUDENT') {
                await Promise.all([
                    this.loadEnrolledCourses(),
                    this.loadAvailableCourses()
                ]);
            } else if (user.role === 'FACULTY') {
                await this.loadFacultyCourses();
            } else if (user.role === 'ADMIN') {
                await this.loadAllCourses();
            }
        } catch (error) {
            console.error('Error loading course data:', error);
            apiHelpers.handleError(error);
        }
    }

    // Load enrolled courses for students
    async loadEnrolledCourses() {
        try {
            const response = await apiHelpers.getStudentCourses();
            this.enrolledCourses = apiHelpers.checkResponse(response);
            this.renderEnrolledCourses();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Load available courses for students
    async loadAvailableCourses() {
        try {
            const response = await apiHelpers.request('/student/courses/available');
            this.availableCourses = apiHelpers.checkResponse(response);
            this.renderAvailableCourses();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Load faculty courses
    async loadFacultyCourses() {
        try {
            const response = await apiHelpers.getFacultyCourses();
            this.courses = apiHelpers.checkResponse(response);
            this.renderFacultyCourses();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Load all courses for admin
    async loadAllCourses() {
        try {
            const response = await apiHelpers.request('/admin/courses');
            this.courses = apiHelpers.checkResponse(response);
            this.renderAllCourses();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Render user info
    renderUserInfo() {
        const userNameElement = document.getElementById('user-name');
        const userRoleElement = document.getElementById('user-role');

        if (userNameElement) userNameElement.textContent = this.currentUser.name;
        if (userRoleElement) userRoleElement.textContent = this.currentUser.role;
    }

    // Render enrolled courses for students
    renderEnrolledCourses() {
        const container = document.getElementById('enrolled-courses-container');
        if (!container) return;

        if (!this.enrolledCourses || this.enrolledCourses.length === 0) {
            container.innerHTML = '<p class="no-data">You are not enrolled in any courses yet.</p>';
            return;
        }

        const coursesHTML = this.enrolledCourses.map(enrollment => {
            const course = enrollment.course;
            return `
                <div class="course-card enrolled">
                    <div class="course-header">
                        <h3>${course.courseName}</h3>
                        <span class="course-code">${course.courseCode}</span>
                    </div>
                    <div class="course-details">
                        <p><strong>Instructor:</strong> ${course.instructor?.user?.name || 'TBA'}</p>
                        <p><strong>Credits:</strong> ${course.credits || 0}</p>
                        <p><strong>Schedule:</strong> ${course.schedule || 'TBA'}</p>
                        <p><strong>Description:</strong> ${course.description || 'No description available'}</p>
                    </div>
                    <div class="course-status">
                        <span class="enrollment-status ${enrollment.status?.toLowerCase()}">${enrollment.status}</span>
                        <span class="grade">${enrollment.grade || 'In Progress'}</span>
                    </div>
                    <div class="course-actions">
                        <button onclick="courseManager.viewCourseDetails('${course.id}')" class="btn btn-primary">
                            <i class="fas fa-eye"></i> View Details
                        </button>
                        <button onclick="courseManager.unenrollFromCourse('${course.id}')" class="btn btn-danger">
                            <i class="fas fa-times"></i> Unenroll
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        container.innerHTML = coursesHTML;
    }

    // Render available courses for students
    renderAvailableCourses() {
        const container = document.getElementById('available-courses-container');
        if (!container) return;

        if (!this.availableCourses || this.availableCourses.length === 0) {
            container.innerHTML = '<p class="no-data">No courses available for enrollment.</p>';
            return;
        }

        const coursesHTML = this.availableCourses.map(course => `
            <div class="course-card available">
                <div class="course-header">
                    <h3>${course.courseName}</h3>
                    <span class="course-code">${course.courseCode}</span>
                </div>
                <div class="course-details">
                    <p><strong>Instructor:</strong> ${course.instructor?.user?.name || 'TBA'}</p>
                    <p><strong>Credits:</strong> ${course.credits || 0}</p>
                    <p><strong>Schedule:</strong> ${course.schedule || 'TBA'}</p>
                    <p><strong>Capacity:</strong> ${course.enrolledCount || 0}/${course.maxCapacity || 'Unlimited'}</p>
                    <p><strong>Description:</strong> ${course.description || 'No description available'}</p>
                </div>
                <div class="course-actions">
                    <button onclick="courseManager.viewCourseDetails('${course.id}')" class="btn btn-secondary">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    <button onclick="courseManager.enrollInCourse('${course.id}')" class="btn btn-success">
                        <i class="fas fa-plus"></i> Enroll
                    </button>
                </div>
            </div>
        `).join('');

        container.innerHTML = coursesHTML;
    }

    // Render faculty courses
    renderFacultyCourses() {
        const container = document.getElementById('faculty-courses-container');
        if (!container) return;

        if (!this.courses || this.courses.length === 0) {
            container.innerHTML = '<p class="no-data">No courses assigned to you.</p>';
            return;
        }

        const coursesHTML = this.courses.map(course => `
            <div class="course-card faculty">
                <div class="course-header">
                    <h3>${course.courseName}</h3>
                    <span class="course-code">${course.courseCode}</span>
                </div>
                <div class="course-details">
                    <p><strong>Credits:</strong> ${course.credits || 0}</p>
                    <p><strong>Schedule:</strong> ${course.schedule || 'TBA'}</p>
                    <p><strong>Enrolled Students:</strong> ${course.enrolledCount || 0}</p>
                    <p><strong>Description:</strong> ${course.description || 'No description available'}</p>
                </div>
                <div class="course-actions">
                    <button onclick="courseManager.viewCourseDetails('${course.id}')" class="btn btn-primary">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    <button onclick="courseManager.manageCourseContent('${course.id}')" class="btn btn-secondary">
                        <i class="fas fa-edit"></i> Manage Content
                    </button>
                    <button onclick="courseManager.viewStudentList('${course.id}')" class="btn btn-info">
                        <i class="fas fa-users"></i> Students
                    </button>
                </div>
            </div>
        `).join('');

        container.innerHTML = coursesHTML;
    }

    // Render all courses for admin
    renderAllCourses() {
        const container = document.getElementById('all-courses-container');
        if (!container) return;

        if (!this.courses || this.courses.length === 0) {
            container.innerHTML = '<p class="no-data">No courses found.</p>';
            return;
        }

        const coursesHTML = this.courses.map(course => `
            <div class="course-card admin">
                <div class="course-header">
                    <h3>${course.courseName}</h3>
                    <span class="course-code">${course.courseCode}</span>
                </div>
                <div class="course-details">
                    <p><strong>Instructor:</strong> ${course.instructor?.user?.name || 'TBA'}</p>
                    <p><strong>Department:</strong> ${course.department?.name || 'N/A'}</p>
                    <p><strong>Credits:</strong> ${course.credits || 0}</p>
                    <p><strong>Enrolled:</strong> ${course.enrolledCount || 0}/${course.maxCapacity || 'Unlimited'}</p>
                    <p><strong>Status:</strong> ${course.isActive ? 'Active' : 'Inactive'}</p>
                </div>
                <div class="course-actions">
                    <button onclick="courseManager.viewCourseDetails('${course.id}')" class="btn btn-primary">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    <button onclick="courseManager.editCourse('${course.id}')" class="btn btn-warning">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button onclick="courseManager.deleteCourse('${course.id}')" class="btn btn-danger">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        `).join('');

        container.innerHTML = coursesHTML;
    }

    // Enroll in course
    async enrollInCourse(courseId) {
        try {
            const response = await apiHelpers.request(`/student/courses/${courseId}/enroll`, {
                method: 'POST'
            });
            
            apiHelpers.checkResponse(response);
            alert('Enrolled successfully!');
            
            // Refresh course data
            await this.loadCourseData();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Unenroll from course
    async unenrollFromCourse(courseId) {
        if (!confirm('Are you sure you want to unenroll from this course?')) {
            return;
        }

        try {
            const response = await apiHelpers.request(`/student/courses/${courseId}/unenroll`, {
                method: 'DELETE'
            });
            
            apiHelpers.checkResponse(response);
            alert('Unenrolled successfully!');
            
            // Refresh course data
            await this.loadCourseData();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // View course details
    viewCourseDetails(courseId) {
        window.location.href = `course-details.html?id=${courseId}`;
    }

    // Manage course content (faculty only)
    manageCourseContent(courseId) {
        window.location.href = `course-content.html?id=${courseId}`;
    }

    // View student list (faculty only)
    viewStudentList(courseId) {
        window.location.href = `course-students.html?id=${courseId}`;
    }

    // Edit course (admin only)
    editCourse(courseId) {
        window.location.href = `course-edit.html?id=${courseId}`;
    }

    // Delete course (admin only)
    async deleteCourse(courseId) {
        if (!confirm('Are you sure you want to delete this course? This action cannot be undone.')) {
            return;
        }

        try {
            const response = await apiHelpers.request(`/admin/courses/${courseId}`, {
                method: 'DELETE'
            });
            
            apiHelpers.checkResponse(response);
            alert('Course deleted successfully!');
            
            // Refresh course data
            await this.loadCourseData();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Handle search
    handleSearch(query) {
        if (!query.trim()) {
            this.showAllCourses();
            return;
        }

        const searchTerm = query.toLowerCase();
        this.filterCourses(course => 
            course.courseName.toLowerCase().includes(searchTerm) ||
            course.courseCode.toLowerCase().includes(searchTerm) ||
            course.description?.toLowerCase().includes(searchTerm)
        );
    }

    // Apply filters
    applyFilters() {
        const departmentFilter = document.getElementById('department-filter');
        const creditFilter = document.getElementById('credit-filter');
        const statusFilter = document.getElementById('status-filter');

        let filteredCourses = [...(this.courses || this.availableCourses || this.enrolledCourses)];

        if (departmentFilter?.value) {
            filteredCourses = filteredCourses.filter(course => 
                course.department?.id === departmentFilter.value
            );
        }

        if (creditFilter?.value) {
            filteredCourses = filteredCourses.filter(course => 
                course.credits === parseInt(creditFilter.value)
            );
        }

        if (statusFilter?.value) {
            filteredCourses = filteredCourses.filter(course => 
                course.isActive === (statusFilter.value === 'active')
            );
        }

        this.renderFilteredCourses(filteredCourses);
    }

    // Filter courses by criteria
    filterCourses(filterFn) {
        const allCourses = this.courses || this.availableCourses || this.enrolledCourses;
        const filtered = allCourses.filter(filterFn);
        this.renderFilteredCourses(filtered);
    }

    // Render filtered courses
    renderFilteredCourses(courses) {
        // This would need to be customized based on the specific container and user role
        // For now, just log the filtered results
        console.log('Filtered courses:', courses);
    }

    // Show all courses
    showAllCourses() {
        this.loadCourseData();
    }

    // Refresh data
    async refreshData() {
        await this.loadCourseData();
        alert('Course data refreshed successfully!');
    }

    // Set active navigation
    setActiveNavigation() {
        const currentPage = window.location.pathname.split("/").pop();
        const navLinks = document.querySelectorAll(".nav-link");

        navLinks.forEach(link => {
            const linkPage = link.getAttribute("href");
            link.classList.remove("active");
            
            if (linkPage === currentPage) {
                link.classList.add("active");
            }
        });
    }
}

// Initialize course manager
document.addEventListener("DOMContentLoaded", function() {
    window.courseManager = new CourseManager();
});

// Export for global access
window.CourseManager = CourseManager;