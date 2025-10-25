// API Configuration for College ERP System
// Centralized API endpoints and utility functions

class APIConfig {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.setupAxiosDefaults();
    }

    setupAxiosDefaults() {
        // Set default base URL
        axios.defaults.baseURL = this.baseURL;
        
        // Request interceptor for authentication
        axios.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('college_erp_token');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );

        // Response interceptor for error handling
        axios.interceptors.response.use(
            (response) => response,
            (error) => {
                if (error.response?.status === 401) {
                    localStorage.clear();
                    window.location.href = 'login.html';
                }
                return Promise.reject(error);
            }
        );
    }

    // Authentication Endpoints
    auth = {
        login: '/auth/login',
        register: '/auth/register',
        forgotPassword: '/auth/forgot-password',
        resetPassword: '/auth/reset-password',
        verifyEmail: '/auth/verify-email',
        refreshToken: '/auth/refresh-token'
    };

    // Admin Endpoints
    admin = {
        // Profile
        profile: '/admin/profile',
        
        // Dashboard
        dashboard: {
            overview: '/admin/dashboard/overview',
            statistics: '/admin/dashboard/statistics',
            analytics: {
                students: '/admin/dashboard/analytics/students',
                faculty: '/admin/dashboard/analytics/faculty',
                courses: '/admin/dashboard/analytics/courses',
                departments: '/admin/dashboard/analytics/departments'
            },
            activities: {
                recent: '/admin/dashboard/activities/recent',
                period: '/admin/dashboard/activities/period'
            },
            system: {
                health: '/admin/dashboard/system/health',
                performance: '/admin/dashboard/performance/summary'
            },
            alerts: {
                academic: '/admin/dashboard/alerts/academic'
            },
            stats: {
                quick: '/admin/dashboard/stats/quick'
            }
        },
        
        // User Management
        users: {
            list: '/admin/users',
            byId: (id) => `/admin/users/${id}`,
            byRole: (role) => `/admin/users/role/${role}`,
            create: '/admin/users',
            update: (id) => `/admin/users/${id}`,
            delete: (id) => `/admin/users/${id}`,
            activate: (id) => `/admin/users/${id}/activate`,
            deactivate: (id) => `/admin/users/${id}/deactivate`
        },
        
        // Student Management
        students: {
            list: '/admin/students',
            byId: (id) => `/admin/students/${id}`,
            create: '/admin/students',
            update: (id) => `/admin/students/${id}`,
            delete: (id) => `/admin/students/${id}`,
            status: (id) => `/admin/students/${id}/status`,
            search: '/admin/students/search'
        },
        
        // Faculty Management
        faculty: {
            list: '/admin/faculty',
            byId: (id) => `/admin/faculty/${id}`,
            create: '/admin/faculty',
            update: (id) => `/admin/faculty/${id}`,
            delete: (id) => `/admin/faculty/${id}`,
            byDepartment: (deptId) => `/admin/faculty/department/${deptId}`
        },
        
        // Department Management
        departments: {
            list: '/admin/departments',
            byId: (id) => `/admin/departments/${id}`,
            create: '/admin/departments',
            update: (id) => `/admin/departments/${id}`,
            delete: (id) => `/admin/departments/${id}`
        },
        
        // Course Management
        courses: {
            list: '/admin/courses',
            byId: (id) => `/admin/courses/${id}`,
            create: '/admin/courses',
            update: (id) => `/admin/courses/${id}`,
            delete: (id) => `/admin/courses/${id}`,
            byDepartment: (deptId) => `/admin/courses/department/${deptId}`
        },
        
        // Statistics
        statistics: {
            userRoles: '/admin/statistics/user-roles',
            coursesAvailable: '/admin/courses/available-enrollment'
        }
    };

    // Student Endpoints
    student = {
        profile: '/student/profile',
        dashboard: '/student/dashboard',
        
        // Courses
        courses: {
            enrolled: '/student/courses/enrolled',
            available: '/student/courses/available',
            search: '/student/courses/search',
            enroll: (courseId) => `/student/courses/${courseId}/enroll`,
            drop: (courseId) => `/student/courses/${courseId}/drop`
        },
        
        // Grades
        grades: {
            current: '/student/grades/current-semester',
            all: '/student/grades/all',
            byCourse: (courseId) => `/student/grades/course/${courseId}`,
            transcript: '/student/grades/transcript'
        },
        
        // Attendance
        attendance: {
            summary: '/student/attendance/summary',
            detailed: '/student/attendance/detailed',
            byCourse: (courseId) => `/student/attendance/course/${courseId}`
        },
        
        // Academic Records
        academic: {
            performance: '/student/academic/performance',
            history: '/student/academic/history',
            calendar: '/student/academic/calendar'
        }
    };

    // Faculty Endpoints
    faculty = {
        profile: '/faculty/profile',
        dashboard: '/faculty/dashboard',
        
        // Course Management
        courses: {
            assigned: '/faculty/courses/assigned',
            byId: (courseId) => `/faculty/courses/${courseId}`,
            enrollments: (courseId) => `/faculty/courses/${courseId}/enrollments`,
            analytics: (courseId) => `/faculty/courses/${courseId}/analytics`
        },
        
        // Student Management
        students: {
            list: '/faculty/students',
            search: '/faculty/students/search',
            byCourse: (courseId) => `/faculty/students/course/${courseId}`,
            byId: (studentId) => `/faculty/students/${studentId}`,
            performance: (studentId) => `/faculty/students/${studentId}/performance`
        },
        
        // Attendance Management
        attendance: {
            record: '/faculty/attendance/record',
            update: (recordId) => `/faculty/attendance/${recordId}`,
            summary: (courseId) => `/faculty/attendance/course/${courseId}/summary`,
            reports: (courseId) => `/faculty/attendance/course/${courseId}/reports`
        },
        
        // Grade Management
        grades: {
            enter: '/faculty/grades/enter',
            update: (gradeId) => `/faculty/grades/${gradeId}`,
            byCourse: (courseId) => `/faculty/grades/course/${courseId}`,
            statistics: (courseId) => `/faculty/grades/course/${courseId}/statistics`
        }
    };

    // Common Endpoints
    common = {
        departments: {
            list: '/departments',
            byId: (id) => `/departments/${id}`,
            byCode: (code) => `/departments/code/${code}`,
            search: '/departments/search',
            count: '/departments/count'
        },
        
        courses: {
            list: '/courses',
            byId: (id) => `/courses/${id}`,
            byCode: (code) => `/courses/code/${code}`,
            byDepartment: (deptId) => `/courses/department/${deptId}`,
            search: '/courses/search',
            count: '/courses/count',
            enrollments: (courseId) => `/courses/${courseId}/enrollments`,
            enrollmentCount: (courseId) => `/courses/${courseId}/enrollment-count`
        },
        
        users: {
            profile: (id) => `/users/profile/${id}`,
            byEmail: (email) => `/users/email/${email}`,
            updateSubjects: (id) => `/users/${id}/subjects`
        },
        
        test: {
            hello: '/test/hello',
            health: '/test/health'
        }
    };
}

// API Helper Functions
class APIHelpers {
    constructor(apiConfig) {
        this.config = apiConfig;
    }

    // Generic API request method
    async request(endpoint, options = {}) {
        try {
            const response = await axios({
                url: endpoint,
                method: options.method || 'GET',
                data: options.data,
                params: options.params,
                ...options
            });
            
            return response.data;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // Authentication helpers
    async login(email, password) {
        return this.request(this.config.auth.login, {
            method: 'POST',
            data: { email, password }
        });
    }

    async register(userData) {
        return this.request(this.config.auth.register, {
            method: 'POST',
            data: userData
        });
    }

    // Admin helpers
    async getAdminDashboard() {
        return this.request(this.config.admin.dashboard.overview);
    }

    async getStudents(page = 0, size = 10) {
        return this.request(this.config.admin.students.list, {
            params: { page, size }
        });
    }

    async createStudent(studentData) {
        return this.request(this.config.admin.students.create, {
            method: 'POST',
            data: studentData
        });
    }

    async updateStudent(id, studentData) {
        return this.request(this.config.admin.students.update(id), {
            method: 'PUT',
            data: studentData
        });
    }

    async deleteStudent(id) {
        return this.request(this.config.admin.students.delete(id), {
            method: 'DELETE'
        });
    }

    // Faculty helpers
    async getFaculty(page = 0, size = 10) {
        return this.request(this.config.admin.faculty.list, {
            params: { page, size }
        });
    }

    async createFaculty(facultyData) {
        return this.request(this.config.admin.faculty.create, {
            method: 'POST',
            data: facultyData
        });
    }

    // Department helpers
    async getDepartments() {
        return this.request(this.config.common.departments.list);
    }

    async createDepartment(deptData) {
        return this.request(this.config.admin.departments.create, {
            method: 'POST',
            data: deptData
        });
    }

    // Course helpers
    async getCourses(page = 0, size = 10) {
        return this.request(this.config.common.courses.list, {
            params: { page, size }
        });
    }

    async createCourse(courseData) {
        return this.request(this.config.admin.courses.create, {
            method: 'POST',
            data: courseData
        });
    }

    // Student portal helpers
    async getStudentProfile() {
        return this.request(this.config.student.profile);
    }

    async getStudentCourses() {
        return this.request(this.config.student.courses.enrolled);
    }

    async getStudentGrades() {
        return this.request(this.config.student.grades.current);
    }

    async getStudentAttendance() {
        return this.request(this.config.student.attendance.summary);
    }

    // Faculty portal helpers
    async getFacultyProfile() {
        return this.request(this.config.faculty.profile);
    }

    async getFacultyCourses() {
        return this.request(this.config.faculty.courses.assigned);
    }

    async getFacultyStudents() {
        return this.request(this.config.faculty.students.list);
    }

    // Error handling helper
    handleError(error) {
        console.error('API Error:', error);
        
        if (error.response) {
            const { status, data } = error.response;
            switch (status) {
                case 401:
                    localStorage.clear();
                    window.location.href = 'login.html';
                    break;
                case 403:
                    alert('Access denied. You do not have permission for this action.');
                    break;
                case 404:
                    alert('Resource not found.');
                    break;
                case 500:
                    alert('Server error. Please try again later.');
                    break;
                default:
                    alert(data?.message || 'An error occurred. Please try again.');
            }
        } else {
            alert('Network error. Please check your connection.');
        }
        
        throw error;
    }

    // Response helper to check for success
    checkResponse(response) {
        if (response.success) {
            return response.data;
        } else {
            throw new Error(response.message || 'Operation failed');
        }
    }
}

// Initialize global API instances
const apiConfig = new APIConfig();
const apiHelpers = new APIHelpers(apiConfig);

// Export for use in other modules
window.API_CONFIG = apiConfig;
window.API_HELPERS = apiHelpers;