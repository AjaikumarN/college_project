// Student Dashboard JavaScript
// Enhanced student portal functionality with proper backend integration

class StudentDashboard {
    constructor() {
        this.currentUser = null;
        this.courses = [];
        this.grades = [];
        this.attendance = [];
        
        this.initializeAuth();
        this.initializeEventListeners();
        this.loadDashboardData();
    }

    // Check authentication and initialize user data
    initializeAuth() {
        if (!authService.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        if (!authService.isStudent()) {
            authService.showAccessDenied();
            return;
        }

        this.currentUser = authService.getCurrentUser();
        this.renderUserInfo();
    }

    // Initialize event listeners
    initializeEventListeners() {
        // Navigation active state
        this.setActiveNavigation();
        
        // Course enrollment
        const enrollButton = document.getElementById('enroll-course-btn');
        if (enrollButton) {
            enrollButton.addEventListener('click', () => this.handleCourseEnrollment());
        }

        // Refresh data buttons
        const refreshButtons = document.querySelectorAll('.refresh-btn');
        refreshButtons.forEach(btn => {
            btn.addEventListener('click', () => this.refreshData());
        });
    }

    // Load all dashboard data
    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadProfile(),
                this.loadCourses(),
                this.loadGrades(),
                this.loadAttendance()
                // Academic calendar endpoint not yet implemented
                // this.loadAcademicCalendar()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    // Load student profile
    async loadProfile() {
        try {
            const response = await apiHelpers.request('/student/profile');
            const profile = apiHelpers.checkResponse(response);
            this.renderProfile(profile);
        } catch (error) {
            console.error('Error loading profile:', error);
            // Don't show error alert, profile is optional
        }
    }

    // Load enrolled courses
    async loadCourses() {
        try {
            const response = await apiHelpers.request('/student/enrollments');
            this.courses = apiHelpers.checkResponse(response);
            this.renderCourses();
        } catch (error) {
            console.error('Error loading courses:', error);
            // Don't show error alert, render empty state instead
            this.courses = [];
            this.renderCourses();
        }
    }

    // Load grades
    async loadGrades() {
        try {
            const response = await apiHelpers.request('/student/portal/grades');
            const gradesData = apiHelpers.checkResponse(response);
            this.grades = gradesData.grades || [];
            this.renderGrades();
        } catch (error) {
            console.error('Error loading grades:', error);
            // Don't show error alert, render empty state instead
            this.grades = [];
            this.renderGrades();
        }
    }

    // Load attendance
    async loadAttendance() {
        try {
            const response = await apiHelpers.request('/student/portal/attendance');
            const attendanceData = apiHelpers.checkResponse(response);
            this.attendance = attendanceData.courseAttendance || [];
            this.renderAttendance();
        } catch (error) {
            console.error('Error loading attendance:', error);
            // Don't show error alert, render empty state instead
            this.attendance = [];
            this.renderAttendance();
        }
    }

    // Load academic calendar
    async loadAcademicCalendar() {
        try {
            const response = await apiHelpers.request('/student/academic/calendar');
            const calendar = apiHelpers.checkResponse(response);
            this.renderAcademicCalendar(calendar);
        } catch (error) {
            console.error('Error loading academic calendar:', error);
        }
    }

    // Render user info in header
    renderUserInfo() {
        const userNameElement = document.getElementById('user-name');
        const userEmailElement = document.getElementById('user-email');
        const userRoleElement = document.getElementById('user-role');

        if (userNameElement) userNameElement.textContent = this.currentUser.name;
        if (userEmailElement) userEmailElement.textContent = this.currentUser.email;
        if (userRoleElement) userRoleElement.textContent = 'Student';
    }

    // Render student profile
    renderProfile(profile) {
        const profileContainer = document.getElementById('profile-container');
        if (!profileContainer) return;

        const profileHTML = `
            <div class="profile-card">
                <div class="profile-header">
                    <div class="profile-avatar">
                        <i class="fas fa-user-graduate"></i>
                    </div>
                    <div class="profile-info">
                        <h3>${profile.user.name}</h3>
                        <p class="student-id">ID: ${profile.studentId || profile.admissionNumber}</p>
                        <p class="department">${profile.department?.name || 'N/A'}</p>
                    </div>
                </div>
                <div class="profile-details">
                    <div class="detail-item">
                        <label>Email:</label>
                        <span>${profile.user.email}</span>
                    </div>
                    <div class="detail-item">
                        <label>Course:</label>
                        <span>${profile.course || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Current Year:</label>
                        <span>${profile.currentYear || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Semester:</label>
                        <span>${profile.currentSemester || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>CGPA:</label>
                        <span>${profile.cgpa ? profile.cgpa.toFixed(2) : 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Status:</label>
                        <span class="status ${profile.status?.toLowerCase()}">${profile.status || 'Active'}</span>
                    </div>
                </div>
            </div>
        `;

        profileContainer.innerHTML = profileHTML;
    }

    // Render enrolled courses
    renderCourses() {
        const coursesContainer = document.getElementById('courses-container');
        if (!coursesContainer) return;

        if (!this.courses || this.courses.length === 0) {
            coursesContainer.innerHTML = '<p class="no-data">No courses enrolled yet.</p>';
            return;
        }

        const coursesHTML = this.courses.map(enrollment => {
            const course = enrollment.course;
            return `
                <div class="course-card">
                    <div class="course-header">
                        <h4>${course.courseName}</h4>
                        <span class="course-code">${course.courseCode}</span>
                    </div>
                    <div class="course-details">
                        <p><strong>Instructor:</strong> ${course.instructor?.user?.name || 'TBA'}</p>
                        <p><strong>Credits:</strong> ${course.credits || 0}</p>
                        <p><strong>Schedule:</strong> ${course.schedule || 'TBA'}</p>
                        <p><strong>Classroom:</strong> ${course.classroom || 'TBA'}</p>
                    </div>
                    <div class="course-status">
                        <span class="enrollment-status ${enrollment.status?.toLowerCase()}">${enrollment.status}</span>
                        <span class="grade">${enrollment.grade || 'In Progress'}</span>
                    </div>
                </div>
            `;
        }).join('');

        coursesContainer.innerHTML = coursesHTML;
    }

    // Render grades
    renderGrades() {
        const gradesContainer = document.getElementById('grades-container');
        if (!gradesContainer) return;

        if (!this.grades || this.grades.length === 0) {
            gradesContainer.innerHTML = '<p class="no-data">No grades available yet.</p>';
            return;
        }

        // Group grades by course
        const gradesByCourse = this.grades.reduce((acc, grade) => {
            const courseId = grade.course.id;
            if (!acc[courseId]) {
                acc[courseId] = {
                    course: grade.course,
                    grades: []
                };
            }
            acc[courseId].grades.push(grade);
            return acc;
        }, {});

        const gradesHTML = Object.values(gradesByCourse).map(courseGrades => {
            const course = courseGrades.course;
            const grades = courseGrades.grades;
            
            const gradesListHTML = grades.map(grade => `
                <div class="grade-item">
                    <span class="assessment">${grade.assessmentName}</span>
                    <span class="marks">${grade.marksObtained}/${grade.maxMarks}</span>
                    <span class="percentage">${grade.percentage ? grade.percentage.toFixed(1) + '%' : 'N/A'}</span>
                    <span class="grade">${grade.grade || 'N/A'}</span>
                </div>
            `).join('');

            return `
                <div class="grades-course-section">
                    <h4>${course.courseName} (${course.courseCode})</h4>
                    <div class="grades-list">
                        <div class="grade-header">
                            <span>Assessment</span>
                            <span>Marks</span>
                            <span>Percentage</span>
                            <span>Grade</span>
                        </div>
                        ${gradesListHTML}
                    </div>
                </div>
            `;
        }).join('');

        gradesContainer.innerHTML = gradesHTML;
    }

    // Render attendance
    renderAttendance() {
        const attendanceContainer = document.getElementById('attendance-container');
        if (!attendanceContainer) return;

        if (!this.attendance || this.attendance.length === 0) {
            attendanceContainer.innerHTML = '<p class="no-data">No attendance records available.</p>';
            return;
        }

        const attendanceHTML = this.attendance.map(courseAttendance => {
            const percentage = courseAttendance.attendancePercentage;
            const statusClass = percentage >= 75 ? 'good' : percentage >= 50 ? 'warning' : 'poor';

            return `
                <div class="attendance-card">
                    <div class="attendance-header">
                        <h4>${courseAttendance.courseName}</h4>
                        <span class="course-code">${courseAttendance.courseCode}</span>
                    </div>
                    <div class="attendance-stats">
                        <div class="stat-item">
                            <label>Attendance:</label>
                            <span class="percentage ${statusClass}">${percentage ? percentage.toFixed(1) + '%' : 'N/A'}</span>
                        </div>
                        <div class="stat-item">
                            <label>Present:</label>
                            <span>${courseAttendance.present || 0}</span>
                        </div>
                        <div class="stat-item">
                            <label>Total Classes:</label>
                            <span>${courseAttendance.totalClasses || 0}</span>
                        </div>
                    </div>
                    <div class="attendance-progress">
                        <div class="progress-bar">
                            <div class="progress-fill ${statusClass}" style="width: ${percentage || 0}%"></div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        attendanceContainer.innerHTML = attendanceHTML;
    }

    // Render academic calendar
    renderAcademicCalendar(calendar) {
        const calendarContainer = document.getElementById('calendar-container');
        if (!calendarContainer || !calendar) return;

        const eventsHTML = calendar.upcomingEvents?.map(event => `
            <div class="calendar-event">
                <div class="event-date">${new Date(event.date).toLocaleDateString()}</div>
                <div class="event-details">
                    <h5>${event.title}</h5>
                    <p>${event.description}</p>
                </div>
            </div>
        `).join('') || '<p class="no-data">No upcoming events.</p>';

        calendarContainer.innerHTML = eventsHTML;
    }

    // Handle course enrollment
    async handleCourseEnrollment() {
        const courseId = prompt('Enter Course ID to enroll:');
        if (!courseId) return;

        try {
            const response = await apiHelpers.request(`/student/courses/${courseId}/enroll`, {
                method: 'POST'
            });
            const result = apiHelpers.checkResponse(response);
            
            alert('Enrolled successfully!');
            this.loadCourses(); // Refresh courses
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Refresh all data
    async refreshData() {
        await this.loadDashboardData();
        alert('Data refreshed successfully!');
    }

    // Set active navigation based on current page
    setActiveNavigation() {
        const currentPage = window.location.pathname.split("/").pop();
        const navLinks = document.querySelectorAll(".nav-link");

        navLinks.forEach(link => {
            const linkPage = link.getAttribute("href");
            link.classList.remove("active");
            
            if (linkPage === currentPage || (currentPage === "" && linkPage === "index.html")) {
                link.classList.add("active");
            }
        });
    }
}

// Initialize student dashboard
document.addEventListener("DOMContentLoaded", function() {
    window.studentDashboard = new StudentDashboard();
});

// Export for global access
window.StudentDashboard = StudentDashboard;