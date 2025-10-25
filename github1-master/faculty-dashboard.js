// Faculty Dashboard JavaScript
// Enhanced faculty portal functionality with proper backend integration

class FacultyDashboard {
    constructor() {
        this.currentUser = null;
        this.courses = [];
        this.students = [];
        this.announcements = [];
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

        if (!authService.isFaculty()) {
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
        
        // Create announcement
        const createAnnouncementBtn = document.getElementById('create-announcement-btn');
        if (createAnnouncementBtn) {
            createAnnouncementBtn.addEventListener('click', () => this.showCreateAnnouncementModal());
        }

        // Mark attendance
        const markAttendanceBtn = document.getElementById('mark-attendance-btn');
        if (markAttendanceBtn) {
            markAttendanceBtn.addEventListener('click', () => this.showAttendanceModal());
        }

        // Grade students
        const gradeStudentsBtn = document.getElementById('grade-students-btn');
        if (gradeStudentsBtn) {
            gradeStudentsBtn.addEventListener('click', () => this.showGradingModal());
        }

        // Refresh data
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
                this.loadCourses()
                // Skip these for now as they're causing 500 errors
                // this.loadStudents(),
                // this.loadAnnouncements(),
                // this.loadAttendanceStats()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    // Load faculty profile
    async loadProfile() {
        try {
            const response = await apiHelpers.request('/faculty/profile');
            console.log('📥 Faculty profile response:', response);
            
            if (response.success && response.data) {
                this.renderProfile(response.data);
            } else {
                console.error('Failed to load profile:', response.message);
            }
        } catch (error) {
            console.error('Error loading faculty profile:', error);
            apiHelpers.handleError(error);
        }
    }

    // Load assigned courses
    async loadCourses() {
        try {
            const response = await apiHelpers.request('/faculty/courses');
            console.log('📚 Faculty courses response:', response);
            
            if (response.success && response.data) {
                this.courses = Array.isArray(response.data) ? response.data : [];
                this.renderCourses();
            } else {
                console.error('Failed to load courses:', response.message);
                this.courses = [];
                this.renderCourses();
            }
        } catch (error) {
            console.error('Error loading courses:', error);
            this.courses = [];
            this.renderCourses();
            apiHelpers.handleError(error);
        }
    }

    // Load students in faculty courses
    async loadStudents() {
        try {
            const response = await apiHelpers.request('/faculty/students');
            console.log('👥 Faculty students response:', response);
            
            if (response.success && response.data) {
                // Handle paginated response
                this.students = response.data.content || response.data || [];
                this.renderStudents();
            } else {
                console.error('Failed to load students:', response.message);
                this.students = [];
                this.renderStudents();
            }
        } catch (error) {
            console.error('Error loading students:', error);
            this.students = [];
            this.renderStudents();
            apiHelpers.handleError(error);
        }
    }

    // Load faculty announcements
    async loadAnnouncements() {
        try {
            const response = await apiHelpers.request('/faculty/announcements');
            console.log('📢 Faculty announcements response:', response);
            
            if (response.success && response.data) {
                this.announcements = Array.isArray(response.data) ? response.data : [];
                this.renderAnnouncements();
            } else {
                console.error('Failed to load announcements:', response.message);
                this.announcements = [];
                this.renderAnnouncements();
            }
        } catch (error) {
            console.error('Error loading announcements:', error);
            this.announcements = [];
            this.renderAnnouncements();
            // Don't show error to user for announcements
        }
    }

    // Load attendance statistics
    async loadAttendanceStats() {
        try {
            const response = await apiHelpers.request('/faculty/attendance/statistics');
            const stats = apiHelpers.checkResponse(response);
            this.renderAttendanceStats(stats);
        } catch (error) {
            console.error('Error loading attendance stats:', error);
        }
    }

    // Render user info in header
    renderUserInfo() {
        const userNameElement = document.getElementById('user-name');
        const userEmailElement = document.getElementById('user-email');
        const userRoleElement = document.getElementById('user-role');

        if (userNameElement) userNameElement.textContent = this.currentUser.name;
        if (userEmailElement) userEmailElement.textContent = this.currentUser.email;
        if (userRoleElement) userRoleElement.textContent = 'Faculty';
    }

    // Render faculty profile
    renderProfile(profile) {
        const profileContainer = document.getElementById('profile-container');
        if (!profileContainer) return;

        const profileHTML = `
            <div class="profile-card">
                <div class="profile-header">
                    <div class="profile-avatar">
                        <i class="fas fa-chalkboard-teacher"></i>
                    </div>
                    <div class="profile-info">
                        <h3>${profile.user.name}</h3>
                        <p class="faculty-id">ID: ${profile.facultyId || profile.employeeId}</p>
                        <p class="department">${profile.department?.name || 'N/A'}</p>
                    </div>
                </div>
                <div class="profile-details">
                    <div class="detail-item">
                        <label>Email:</label>
                        <span>${profile.user.email}</span>
                    </div>
                    <div class="detail-item">
                        <label>Designation:</label>
                        <span>${profile.designation || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Experience:</label>
                        <span>${profile.experience || 'N/A'} years</span>
                    </div>
                    <div class="detail-item">
                        <label>Qualification:</label>
                        <span>${profile.qualification || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <label>Specialization:</label>
                        <span>${profile.specialization || 'N/A'}</span>
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

    // Render assigned courses
    renderCourses() {
        const coursesContainer = document.getElementById('courses-container');
        if (!coursesContainer) return;

        if (!this.courses || this.courses.length === 0) {
            coursesContainer.innerHTML = '<p class="no-data">No courses assigned yet.</p>';
            return;
        }

        const coursesHTML = this.courses.map(course => `
            <div class="course-card">
                <div class="course-header">
                    <h4>${course.courseName}</h4>
                    <span class="course-code">${course.courseCode}</span>
                </div>
                <div class="course-details">
                    <p><strong>Credits:</strong> ${course.credits || 0}</p>
                    <p><strong>Schedule:</strong> ${course.schedule || 'TBA'}</p>
                    <p><strong>Classroom:</strong> ${course.classroom || 'TBA'}</p>
                    <p><strong>Enrolled Students:</strong> ${course.enrolledCount || 0}</p>
                </div>
                <div class="course-actions">
                    <button onclick="facultyDashboard.viewCourseDetails('${course.id}')" class="btn btn-primary">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    <button onclick="facultyDashboard.markAttendance('${course.id}')" class="btn btn-success">
                        <i class="fas fa-check"></i> Mark Attendance
                    </button>
                    <button onclick="facultyDashboard.gradeStudents('${course.id}')" class="btn btn-warning">
                        <i class="fas fa-star"></i> Grade Students
                    </button>
                </div>
            </div>
        `).join('');

        coursesContainer.innerHTML = coursesHTML;
    }

    // Render students
    renderStudents() {
        const studentsContainer = document.getElementById('students-container');
        if (!studentsContainer) return;

        if (!this.students || this.students.length === 0) {
            studentsContainer.innerHTML = '<p class="no-data">No students found.</p>';
            return;
        }

        const studentsHTML = this.students.map(student => `
            <div class="student-card">
                <div class="student-header">
                    <h4>${student.user.name}</h4>
                    <span class="student-id">${student.studentId || student.admissionNumber}</span>
                </div>
                <div class="student-details">
                    <p><strong>Email:</strong> ${student.user.email}</p>
                    <p><strong>Course:</strong> ${student.course || 'N/A'}</p>
                    <p><strong>Year:</strong> ${student.currentYear || 'N/A'}</p>
                    <p><strong>CGPA:</strong> ${student.cgpa ? student.cgpa.toFixed(2) : 'N/A'}</p>
                </div>
                <div class="student-actions">
                    <button onclick="facultyDashboard.viewStudentProfile('${student.id}')" class="btn btn-info">
                        <i class="fas fa-user"></i> View Profile
                    </button>
                </div>
            </div>
        `).join('');

        studentsContainer.innerHTML = studentsHTML;
    }

    // Render announcements
    renderAnnouncements() {
        const announcementsContainer = document.getElementById('announcements-container');
        if (!announcementsContainer) return;

        if (!this.announcements || this.announcements.length === 0) {
            announcementsContainer.innerHTML = '<p class="no-data">No announcements yet.</p>';
            return;
        }

        const announcementsHTML = this.announcements.map(announcement => `
            <div class="announcement-card">
                <div class="announcement-header">
                    <h4>${announcement.title}</h4>
                    <span class="announcement-date">${new Date(announcement.createdAt).toLocaleDateString()}</span>
                </div>
                <div class="announcement-content">
                    <p>${announcement.content}</p>
                    ${announcement.course ? `<p class="course-tag">Course: ${announcement.course.courseName}</p>` : ''}
                </div>
                <div class="announcement-actions">
                    <button onclick="facultyDashboard.editAnnouncement('${announcement.id}')" class="btn btn-secondary">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button onclick="facultyDashboard.deleteAnnouncement('${announcement.id}')" class="btn btn-danger">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        `).join('');

        announcementsContainer.innerHTML = announcementsHTML;
    }

    // Render attendance statistics
    renderAttendanceStats(stats) {
        const statsContainer = document.getElementById('attendance-stats-container');
        if (!statsContainer || !stats) return;

        const statsHTML = `
            <div class="stats-grid">
                <div class="stat-card">
                    <h4>Total Classes</h4>
                    <span class="stat-value">${stats.totalClasses || 0}</span>
                </div>
                <div class="stat-card">
                    <h4>Average Attendance</h4>
                    <span class="stat-value">${stats.averageAttendance ? stats.averageAttendance.toFixed(1) + '%' : 'N/A'}</span>
                </div>
                <div class="stat-card">
                    <h4>Students Below 75%</h4>
                    <span class="stat-value warning">${stats.studentsBelow75 || 0}</span>
                </div>
                <div class="stat-card">
                    <h4>Perfect Attendance</h4>
                    <span class="stat-value good">${stats.perfectAttendance || 0}</span>
                </div>
            </div>
        `;

        statsContainer.innerHTML = statsHTML;
    }

    // Show create announcement modal
    showCreateAnnouncementModal() {
        const title = prompt('Enter announcement title:');
        if (!title) return;

        const content = prompt('Enter announcement content:');
        if (!content) return;

        const courseId = prompt('Enter course ID (leave empty for general announcement):');
        
        this.createAnnouncement(title, content, courseId);
    }

    // Create announcement
    async createAnnouncement(title, content, courseId = null) {
        try {
            const data = { title, content };
            if (courseId) data.courseId = courseId;

            const response = await apiHelpers.request('/faculty/announcements', {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            const result = apiHelpers.checkResponse(response);
            alert('Announcement created successfully!');
            this.loadAnnouncements();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Show attendance modal
    showAttendanceModal() {
        const courseId = prompt('Enter course ID:');
        if (!courseId) return;

        this.markAttendance(courseId);
    }

    // Mark attendance for course
    async markAttendance(courseId) {
        try {
            // This would typically show a modal with student list
            // For now, using simple prompts
            const date = prompt('Enter date (YYYY-MM-DD):') || new Date().toISOString().split('T')[0];
            const attendanceData = prompt('Enter attendance data (JSON format with student IDs and status):');
            
            if (!attendanceData) return;

            const data = {
                date: date,
                attendanceRecords: JSON.parse(attendanceData)
            };

            const response = await apiHelpers.request(`/faculty/courses/${courseId}/attendance`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            const result = apiHelpers.checkResponse(response);
            alert('Attendance marked successfully!');
            this.loadAttendanceStats();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Show grading modal
    showGradingModal() {
        const courseId = prompt('Enter course ID:');
        if (!courseId) return;

        this.gradeStudents(courseId);
    }

    // Grade students for course
    async gradeStudents(courseId) {
        try {
            const assessmentName = prompt('Enter assessment name:');
            if (!assessmentName) return;

            const maxMarks = parseInt(prompt('Enter maximum marks:'));
            if (!maxMarks) return;

            const gradesData = prompt('Enter grades data (JSON format with student IDs and marks):');
            if (!gradesData) return;

            const data = {
                assessmentName: assessmentName,
                maxMarks: maxMarks,
                grades: JSON.parse(gradesData)
            };

            const response = await apiHelpers.request(`/faculty/courses/${courseId}/grades`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            const result = apiHelpers.checkResponse(response);
            alert('Grades submitted successfully!');
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // View course details
    async viewCourseDetails(courseId) {
        window.location.href = `course-details.html?id=${courseId}`;
    }

    // View student profile
    async viewStudentProfile(studentId) {
        window.location.href = `student-profile.html?id=${studentId}`;
    }

    // Edit announcement
    async editAnnouncement(announcementId) {
        const newTitle = prompt('Enter new title:');
        if (!newTitle) return;

        const newContent = prompt('Enter new content:');
        if (!newContent) return;

        try {
            const data = { title: newTitle, content: newContent };

            const response = await apiHelpers.request(`/faculty/announcements/${announcementId}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
            
            const result = apiHelpers.checkResponse(response);
            alert('Announcement updated successfully!');
            this.loadAnnouncements();
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Delete announcement
    async deleteAnnouncement(announcementId) {
        if (!confirm('Are you sure you want to delete this announcement?')) return;

        try {
            const response = await apiHelpers.request(`/faculty/announcements/${announcementId}`, {
                method: 'DELETE'
            });
            
            apiHelpers.checkResponse(response);
            alert('Announcement deleted successfully!');
            this.loadAnnouncements();
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

// Initialize faculty dashboard
document.addEventListener("DOMContentLoaded", function() {
    window.facultyDashboard = new FacultyDashboard();
});

// Export for global access
window.FacultyDashboard = FacultyDashboard;