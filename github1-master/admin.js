
// Enhanced Admin Dashboard JavaScript
// Updated to use correct backend API endpoints

class AdminDashboard {
    constructor() {
        this.initializeEventListeners();
        this.loadDashboardData();
    }

    // Initialize all event listeners
    initializeEventListeners() {
        // Check authentication
        if (!authService.isAuthenticated() || !authService.isAdmin()) {
            window.location.href = 'login.html';
            return;
        }

        // Student form submission
        const studentForm = document.getElementById('studentForm');
        if (studentForm) {
            studentForm.addEventListener('submit', (e) => this.handleStudentCreate(e));
        }

        // Student edit form submission
        const studentEditForm = document.getElementById('studentEditForm');
        if (studentEditForm) {
            studentEditForm.addEventListener('submit', (e) => this.handleStudentUpdate(e));
        }

        // Delete student buttons (delegated event handling)
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('delete-student-btn')) {
                this.handleStudentDelete(e.target.dataset.studentId);
            }
        });

        // Load students on page load
        this.loadStudents();
    }

    // Load dashboard overview data
    async loadDashboardData() {
        try {
            const dashboardData = await apiHelpers.getAdminDashboard();
            this.renderDashboardStats(dashboardData);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    // Load and display students
    async loadStudents() {
        try {
            const response = await apiHelpers.getStudents();
            const students = apiHelpers.checkResponse(response);
            this.renderStudents(students);
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Handle student creation
    async handleStudentCreate(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const studentData = {
            // Map form fields to backend expected format
            admissionNumber: formData.get('studentRoll'),
            user: {
                name: formData.get('studentName'),
                email: formData.get('studentEmail'),
                phone: formData.get('studentPhone'),
                gender: formData.get('studentGender'),
                dob: formData.get('studentDOB'),
                role: 'STUDENT'
            },
            currentYear: parseInt(formData.get('studentClass')),
            course: formData.get('studentCourse'),
            departmentId: formData.get('departmentId')
        };

        try {
            const response = await apiHelpers.createStudent(studentData);
            const newStudent = apiHelpers.checkResponse(response);
            
            alert("Student added successfully!");
            e.target.reset();
            this.loadStudents(); // Refresh the list
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Handle student update
    async handleStudentUpdate(e) {
        e.preventDefault();
        
        const studentId = document.getElementById("editUserId").value;
        const formData = new FormData(e.target);
        
        const updatedData = {
            admissionNumber: formData.get('editStudentRoll'),
            user: {
                name: formData.get('editStudentName'),
                email: formData.get('editStudentEmail'),
                phone: formData.get('editStudentPhone'),
                gender: formData.get('editStudentGender'),
                dob: formData.get('editStudentDob')
            },
            currentYear: parseInt(formData.get('editStudentYear')),
            course: formData.get('editStudentClass')
        };

        try {
            const response = await apiHelpers.updateStudent(studentId, updatedData);
            const updatedStudent = apiHelpers.checkResponse(response);
            
            alert("Student updated successfully!");
            this.closeStudentEdit();
            this.loadStudents(); // Refresh the list
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Handle student deletion
    async handleStudentDelete(studentId) {
        if (!confirm('Are you sure you want to delete this student?')) {
            return;
        }

        try {
            const response = await apiHelpers.deleteStudent(studentId);
            apiHelpers.checkResponse(response);
            
            alert("Student deleted successfully!");
            this.loadStudents(); // Refresh the list
        } catch (error) {
            apiHelpers.handleError(error);
        }
    }

    // Render dashboard statistics
    renderDashboardStats(data) {
        if (data && data.statistics) {
            const stats = data.statistics;
            
            // Update stat cards if they exist
            this.updateStatCard('total-students', stats.totalStudents);
            this.updateStatCard('total-faculty', stats.totalFaculty);
            this.updateStatCard('total-courses', stats.totalCourses);
            this.updateStatCard('total-departments', stats.totalDepartments);
        }
    }

    // Update individual stat card
    updateStatCard(elementId, value) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = value || '0';
        }
    }

    // Render students table
    renderStudents(students) {
        const studentsTableBody = document.getElementById('studentsTableBody');
        if (!studentsTableBody) return;

        if (!students || students.length === 0) {
            studentsTableBody.innerHTML = '<tr><td colspan="8">No students found</td></tr>';
            return;
        }

        const studentsHTML = students.map(student => {
            const user = student.user || {};
            return `
                <tr>
                    <td>${student.admissionNumber || 'N/A'}</td>
                    <td>${user.name || 'N/A'}</td>
                    <td>${user.email || 'N/A'}</td>
                    <td>${student.course || 'N/A'}</td>
                    <td>${student.currentYear || 'N/A'}</td>
                    <td>${user.phone || 'N/A'}</td>
                    <td>${user.gender || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="adminDashboard.openStudentEdit(${student.id})">
                            Edit
                        </button>
                        <button class="btn btn-sm btn-danger delete-student-btn" data-student-id="${student.id}">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

        studentsTableBody.innerHTML = studentsHTML;
    }

    // Open student edit modal
    openStudentEdit(studentId) {
        // This would populate the edit form with student data
        // Implementation depends on your modal structure
        console.log('Opening edit modal for student:', studentId);
    }

    // Close student edit modal
    closeStudentEdit() {
        // Hide the edit modal
        const modal = document.getElementById('editStudentModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }
}

// Initialize admin dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.adminDashboard = new AdminDashboard();
});

// Additional admin utility functions

// Load and render departments for dropdowns
async function loadDepartments() {
    try {
        const response = await apiHelpers.getDepartments();
        const departments = apiHelpers.checkResponse(response);
        
        const departmentSelects = document.querySelectorAll('.department-select');
        departmentSelects.forEach(select => {
            select.innerHTML = '<option value="">Select Department</option>';
            departments.forEach(dept => {
                select.innerHTML += `<option value="${dept.id}">${dept.name}</option>`;
            });
        });
    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

// Load and render faculty for dropdowns
async function loadFaculty() {
    try {
        const response = await apiHelpers.getFaculty();
        const faculty = apiHelpers.checkResponse(response);
        
        const facultySelects = document.querySelectorAll('.faculty-select');
        facultySelects.forEach(select => {
            select.innerHTML = '<option value="">Select Faculty</option>';
            faculty.content.forEach(fac => {
                select.innerHTML += `<option value="${fac.id}">${fac.user.name}</option>`;
            });
        });
    } catch (error) {
        console.error('Error loading faculty:', error);
    }
}

// Load and render courses
async function loadCourses() {
    try {
        const response = await apiHelpers.getCourses();
        const courses = apiHelpers.checkResponse(response);
        
        const courseSelects = document.querySelectorAll('.course-select');
        courseSelects.forEach(select => {
            select.innerHTML = '<option value="">Select Course</option>';
            courses.content.forEach(course => {
                select.innerHTML += `<option value="${course.id}">${course.courseName}</option>`;
            });
        });
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

// Generate reports
async function generateReport(reportType) {
    const reportOutput = document.getElementById('reportOutput');
    if (!reportOutput) return;

    reportOutput.innerHTML = '<div class="loading">Generating report...</div>';

    try {
        let reportData;
        let reportHTML = '';

        switch(reportType) {
            case 'attendance':
                reportData = await apiHelpers.get('/admin/reports/attendance');
                reportHTML = generateAttendanceReport(reportData);
                break;
            case 'marks':
                reportData = await apiHelpers.get('/admin/reports/grades');
                reportHTML = generateMarksReport(reportData);
                break;
            case 'enrollment':
                reportData = await apiHelpers.get('/admin/reports/enrollments');
                reportHTML = generateEnrollmentReport(reportData);
                break;
            case 'performance':
                reportData = await apiHelpers.get('/admin/reports/performance');
                reportHTML = generatePerformanceReport(reportData);
                break;
            default:
                reportHTML = '<p>Report type not supported yet.</p>';
        }

        reportOutput.innerHTML = reportHTML;
    } catch (error) {
        console.error('Error generating report:', error);
        reportOutput.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <p>Unable to generate report. This feature is coming soon!</p>
            </div>
        `;
    }
}

// Generate attendance report HTML
function generateAttendanceReport(data) {
    return `
        <div class="report-container">
            <h4>📊 Attendance Report</h4>
            <p>This feature will display comprehensive attendance statistics across all courses and students.</p>
            <p><strong>Coming Soon:</strong> Course-wise attendance, student-wise attendance, date range filters, and export options.</p>
        </div>
    `;
}

// Generate marks report HTML
function generateMarksReport(data) {
    return `
        <div class="report-container">
            <h4>📊 Marks Report</h4>
            <p>This feature will display grade distributions, average scores, and performance trends.</p>
            <p><strong>Coming Soon:</strong> Course-wise grades, student rankings, grade analytics, and export options.</p>
        </div>
    `;
}

// Generate enrollment report HTML
function generateEnrollmentReport(data) {
    return `
        <div class="report-container">
            <h4>📊 Enrollment Report</h4>
            <p>This feature will display enrollment statistics across departments and courses.</p>
            <p><strong>Coming Soon:</strong> Department-wise enrollments, course capacity utilization, and trend analysis.</p>
        </div>
    `;
}

// Generate performance report HTML
function generatePerformanceReport(data) {
    return `
        <div class="report-container">
            <h4>📊 Performance Report</h4>
            <p>This feature will display overall academic performance metrics and insights.</p>
            <p><strong>Coming Soon:</strong> Student performance trends, department comparisons, and predictive analytics.</p>
        </div>
    `;
}

// Initialize dropdowns on page load
document.addEventListener('DOMContentLoaded', function() {
    loadDepartments();
    loadFaculty();
    loadCourses();
});
