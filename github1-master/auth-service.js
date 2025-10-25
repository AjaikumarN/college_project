// Authentication Service
// Enhanced authentication with JWT token management and role-based access

class AuthService {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.tokenKey = 'college_erp_token';
        this.userKey = 'college_erp_user';
        this.refreshTokenKey = 'college_erp_refresh_token';
        
        // Initialize axios interceptors
        this.initializeInterceptors();
    }

    // Initialize axios request/response interceptors
    initializeInterceptors() {
        // Request interceptor to add auth token
        axios.interceptors.request.use(
            (config) => {
                const token = this.getToken();
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => {
                return Promise.reject(error);
            }
        );

        // Response interceptor to handle token refresh and errors
        axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;

                // Handle 401 Unauthorized
                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;

                    try {
                        await this.refreshToken();
                        // Retry the original request with new token
                        const token = this.getToken();
                        originalRequest.headers.Authorization = `Bearer ${token}`;
                        return axios(originalRequest);
                    } catch (refreshError) {
                        // Refresh failed, logout user
                        this.logout();
                        window.location.href = 'login.html';
                        return Promise.reject(refreshError);
                    }
                }

                return Promise.reject(error);
            }
        );
    }

    // Login user with email and password
    async login(email, password) {
        try {
            const response = await axios.post(`${this.baseURL}/auth/login`, {
                email,
                password
            });

            // Backend returns: { success, message, data: LoginResponse }
            const apiResponse = response.data;
            
            if (!apiResponse.success) {
                throw new Error(apiResponse.message || 'Login failed');
            }

            const loginData = apiResponse.data;
            const token = loginData.accessToken;
            
            // Create user object from LoginResponse
            const user = {
                id: loginData.id,
                name: loginData.name,
                email: loginData.email,
                role: loginData.role.toUpperCase(), // Convert to uppercase for consistency
                course: loginData.course,
                year: loginData.year,
                semester: loginData.semester,
                phone: loginData.phone,
                gender: loginData.gender,
                isVerified: loginData.isVerified,
                isActive: loginData.isActive
            };

            // Store tokens and user data
            this.setToken(token);
            this.setCurrentUser(user);

            return { success: true, user, token };
        } catch (error) {
            console.error('Login error:', error);
            throw new Error(error.response?.data?.message || error.message || 'Login failed');
        }
    }

    // Register new user
    async register(userData) {
        try {
            const response = await axios.post(`${this.baseURL}/auth/register`, userData);
            return { success: true, message: 'Registration successful', data: response.data };
        } catch (error) {
            console.error('Registration error:', error);
            
            // Extract detailed error message
            let errorMessage = 'Registration failed';
            
            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.response?.data?.errors) {
                // Handle validation errors
                const errors = error.response.data.errors;
                errorMessage = Object.values(errors).join(', ');
            } else if (error.response?.status === 409) {
                errorMessage = 'Email already exists';
            } else if (error.response?.status === 400) {
                errorMessage = 'Invalid registration data. Please check all fields.';
            }
            
            throw new Error(errorMessage);
        }
    }

    // Refresh access token
    async refreshToken() {
        try {
            const refreshToken = this.getRefreshToken();
            if (!refreshToken) {
                throw new Error('No refresh token available');
            }

            const response = await axios.post(`${this.baseURL}/auth/refresh`, {
                refreshToken
            });

            const { token, refreshToken: newRefreshToken } = response.data;

            // Update stored tokens
            this.setToken(token);
            if (newRefreshToken) {
                this.setRefreshToken(newRefreshToken);
            }

            return token;
        } catch (error) {
            console.error('Token refresh error:', error);
            throw error;
        }
    }

    // Logout user
    logout() {
        try {
            // Clear stored data
            localStorage.removeItem(this.tokenKey);
            localStorage.removeItem(this.userKey);
            localStorage.removeItem(this.refreshTokenKey);

            // Optional: Call logout endpoint to invalidate token on server
            const token = this.getToken();
            if (token) {
                axios.post(`${this.baseURL}/auth/logout`, {}, {
                    headers: { Authorization: `Bearer ${token}` }
                }).catch(error => console.error('Logout error:', error));
            }
        } catch (error) {
            console.error('Logout error:', error);
        }
    }

    // Check if user is authenticated
    isAuthenticated() {
        const token = this.getToken();
        const user = this.getCurrentUser();
        
        if (!token || !user) {
            return false;
        }

        // Check if token is expired
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            
            if (payload.exp < currentTime) {
                // Token is expired, try to refresh
                this.refreshToken().catch(() => {
                    this.logout();
                });
                return false;
            }
            
            return true;
        } catch (error) {
            console.error('Token validation error:', error);
            return false;
        }
    }

    // Get current user
    getCurrentUser() {
        try {
            const user = localStorage.getItem(this.userKey);
            if (!user || user === 'undefined') {
                return null;
            }
            return JSON.parse(user);
        } catch (error) {
            console.error('Error getting current user:', error);
            // Clear invalid data
            localStorage.removeItem(this.userKey);
            return null;
        }
    }

    // Set current user
    setCurrentUser(user) {
        localStorage.setItem(this.userKey, JSON.stringify(user));
    }

    // Get stored token
    getToken() {
        return localStorage.getItem(this.tokenKey);
    }

    // Set token
    setToken(token) {
        localStorage.setItem(this.tokenKey, token);
    }

    // Get refresh token
    getRefreshToken() {
        return localStorage.getItem(this.refreshTokenKey);
    }

    // Set refresh token
    setRefreshToken(refreshToken) {
        localStorage.setItem(this.refreshTokenKey, refreshToken);
    }

    // Check if user is admin
    isAdmin() {
        const user = this.getCurrentUser();
        return user && (user.role || '').toUpperCase() === 'ADMIN';
    }

    // Check if user is faculty
    isFaculty() {
        const user = this.getCurrentUser();
        return user && (user.role || '').toUpperCase() === 'FACULTY';
    }

    // Check if user is student
    isStudent() {
        const user = this.getCurrentUser();
        return user && (user.role || '').toUpperCase() === 'STUDENT';
    }

    // Check if user has specific role
    hasRole(role) {
        const user = this.getCurrentUser();
        return user && (user.role || '').toUpperCase() === (role || '').toUpperCase();
    }

    // Check if user has any of the specified roles
    hasAnyRole(roles) {
        const user = this.getCurrentUser();
        if (!user) return false;
        const userRole = (user.role || '').toUpperCase();
        return roles.some(role => (role || '').toUpperCase() === userRole);
    }

    // Get user permissions
    getUserPermissions() {
        const user = this.getCurrentUser();
        if (!user) return [];

        // Define role-based permissions
        const permissions = {
            ADMIN: [
                'manage_users',
                'manage_courses',
                'manage_departments',
                'view_all_data',
                'manage_system_settings',
                'generate_reports'
            ],
            FACULTY: [
                'view_assigned_courses',
                'manage_course_content',
                'grade_students',
                'mark_attendance',
                'create_announcements',
                'view_student_profiles'
            ],
            STUDENT: [
                'view_courses',
                'view_grades',
                'view_attendance',
                'enroll_courses',
                'view_announcements',
                'update_profile'
            ]
        };

        return permissions[user.role] || [];
    }

    // Check if user has specific permission
    hasPermission(permission) {
        const userPermissions = this.getUserPermissions();
        return userPermissions.includes(permission);
    }

    // Redirect based on user role
    redirectToRoleDashboard() {
        const user = this.getCurrentUser();
        console.log('Redirecting user:', user);
        
        if (!user) {
            console.log('No user found, redirecting to login');
            window.location.href = 'login.html';
            return;
        }

        // Normalize role to uppercase for comparison
        const role = (user.role || '').toUpperCase();
        console.log('User role (normalized):', role);
        
        switch (role) {
            case 'ADMIN':
                console.log('Redirecting to admin.html');
                window.location.href = 'admin.html';
                break;
            case 'FACULTY':
                console.log('Redirecting to faculty.html');
                window.location.href = 'faculty.html';
                break;
            case 'STUDENT':
                console.log('Redirecting to student.html');
                window.location.href = 'student.html';
                break;
            default:
                console.error('Unknown role:', role);
                window.location.href = 'index.html';
        }
    }

    // Show access denied message
    showAccessDenied() {
        alert('Access Denied: You do not have permission to access this page.');
        this.redirectToRoleDashboard();
    }

    // Forgot password
    async forgotPassword(email) {
        try {
            const response = await axios.post(`${this.baseURL}/auth/forgot-password`, {
                email
            });
            return { success: true, message: 'Password reset email sent' };
        } catch (error) {
            console.error('Forgot password error:', error);
            throw new Error(error.response?.data?.message || 'Failed to send reset email');
        }
    }

    // Reset password
    async resetPassword(token, newPassword) {
        try {
            const response = await axios.post(`${this.baseURL}/auth/reset-password`, {
                token,
                newPassword
            });
            return { success: true, message: 'Password reset successful' };
        } catch (error) {
            console.error('Reset password error:', error);
            throw new Error(error.response?.data?.message || 'Password reset failed');
        }
    }

    // Change password
    async changePassword(currentPassword, newPassword) {
        try {
            const response = await axios.put(`${this.baseURL}/auth/change-password`, {
                currentPassword,
                newPassword
            });
            return { success: true, message: 'Password changed successfully' };
        } catch (error) {
            console.error('Change password error:', error);
            throw new Error(error.response?.data?.message || 'Password change failed');
        }
    }

    // Update profile
    async updateProfile(profileData) {
        try {
            const response = await axios.put(`${this.baseURL}/auth/profile`, profileData);
            const updatedUser = response.data;
            
            // Update stored user data
            this.setCurrentUser(updatedUser);
            
            return { success: true, user: updatedUser };
        } catch (error) {
            console.error('Update profile error:', error);
            throw new Error(error.response?.data?.message || 'Profile update failed');
        }
    }

    // Verify email
    async verifyEmail(token) {
        try {
            const response = await axios.post(`${this.baseURL}/auth/verify-email`, {
                token
            });
            return { success: true, message: 'Email verified successfully' };
        } catch (error) {
            console.error('Email verification error:', error);
            throw new Error(error.response?.data?.message || 'Email verification failed');
        }
    }

    // Resend verification email
    async resendVerificationEmail() {
        try {
            const response = await axios.post(`${this.baseURL}/auth/resend-verification`);
            return { success: true, message: 'Verification email sent' };
        } catch (error) {
            console.error('Resend verification error:', error);
            throw new Error(error.response?.data?.message || 'Failed to send verification email');
        }
    }

    // Get user activity log
    async getUserActivityLog() {
        try {
            const response = await axios.get(`${this.baseURL}/auth/activity-log`);
            return response.data;
        } catch (error) {
            console.error('Activity log error:', error);
            throw new Error('Failed to fetch activity log');
        }
    }
}

// Create global instance
const authService = new AuthService();

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthService;
}